import axios from 'axios';

const API_BASE_URL = '/api/schedules';

const handleApiError = (error, defaultMessage = 'API 호출 중 오류가 발생했습니다.') => {
  console.error('API Error:', error);

  if (error.response) {
    const { status, data } = error.response;

    if (status === 400) {
      return data.message || data.errors || '잘못된 요청입니다.';
    } else if (status === 401) {
      return '인증이 필요합니다. 다시 로그인해주세요.';
    } else if (status === 403) {
      return '권한이 없습니다.';
    } else if (status === 404) {
      return '요청한 일정을 찾을 수 없습니다.';
    } else if (status === 500) {
      return '서버 내부 오류가 발생했습니다.';
    }

    return data.message || defaultMessage;
  } else if (error.request) {
    return '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.';
  } else {
    return error.message || defaultMessage;
  }
};

// 🔧 getCurrentUser 함수 제거
// const getCurrentUser = () => {
//   return {
//     employeeId: 'TEMP_USER_001',
//     name: '홍길동',
//     deptId: 'DEPT_001'
//   };
// };

const formatDateTimeForBackend = (dateTimeString) => {
  if (!dateTimeString) return null;

  if (dateTimeString.length === 16 && dateTimeString.includes('T')) {
    const formatted = dateTimeString + ':00';
    console.log(`날짜 형식 변환: ${dateTimeString} → ${formatted}`);
    return formatted;
  }

  return dateTimeString;
};

const validateAndNormalizeScheduleData = (scheduleData) => {
  const normalized = {
    title: scheduleData.title || '',
    description: scheduleData.description || '',
    startDate: scheduleData.startDate || '',
    endDate: scheduleData.endDate || '',
    visibility: scheduleData.visibility || 'PUBLIC',
    alarmEnabled: Boolean(scheduleData.alarmEnabled),
    alarmTime: scheduleData.alarmTime || null,
    selectedParticipants: Array.isArray(scheduleData.selectedParticipants)
      ? scheduleData.selectedParticipants
      : []
  };

  console.log('데이터 정규화 결과:', normalized);
  return normalized;
};


function getFieldDisplayName(fieldName) {
  const fieldMap = {
    'title': '일정 제목',
    'description': '일정 설명',
    'startDate': '시작일시',
    'endDate': '종료일시',
    'visibility': '공개범위',
    'alarmEnabled': '알림설정',
    'alarmTime': '알림시간',
    'participantIds': '참여자'
  };

  return fieldMap[fieldName] || fieldName;
}


const scheduleApiService = {
  // 🔧 employeeId 인자 추가
  async registerSchedule(scheduleData, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      const normalizedData = validateAndNormalizeScheduleData(scheduleData);
      console.log('정규화된 일정 데이터:', normalizedData);

      const originalVisibility = normalizedData.visibility;
      let backendVisibility;

      if (!originalVisibility || typeof originalVisibility !== 'string') {
        console.warn('유효하지 않은 visibility 값, 기본값 PUBLIC 사용:', originalVisibility);
        backendVisibility = 'PUBLIC';
      } else {
        backendVisibility = originalVisibility === 'DEPARTMENT' ? 'GROUP' : originalVisibility.toUpperCase();
        if (originalVisibility !== backendVisibility) {
          console.log(`공개범위 변환: ${originalVisibility} → ${backendVisibility}`);
        }
      }

      const requestDto = {
        title: normalizedData.title,
        description: normalizedData.description,
        startDate: formatDateTimeForBackend(normalizedData.startDate),
        endDate: formatDateTimeForBackend(normalizedData.endDate),
        visibility: backendVisibility,
        alarmEnabled: normalizedData.alarmEnabled,
        alarmTime: normalizedData.alarmEnabled
          ? formatDateTimeForBackend(normalizedData.alarmTime)
          : null,
        participantIds: normalizedData.selectedParticipants?.map(p => p.employeeId) || []
      };

      console.log('일정 등록 요청 데이터:', requestDto);

      const response = await axios.post(API_BASE_URL, requestDto, {
        headers: {
          'Content-Type': 'application/json',
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일정 등록 성공:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 등록 실패:', error);

      if (error.response?.status === 400 && error.response?.data?.errors) {
        const validationErrors = error.response.data.errors;
        console.log('유효성 검사 에러:', validationErrors);

        const errorMessages = validationErrors.map(err =>
          `${getFieldDisplayName(err.field)}: ${err.message}`
        ).join('\n');

        throw new Error(errorMessages);
      }

      const errorMessage = handleApiError(error, '일정 등록에 실패했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 employeeId 인자 추가
  async getMonthlySchedules(year, month, employeeId = null) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거
      const targetEmployeeId = employeeId; // 🔧 인자로 받은 employeeId 사용

      console.log(`월별 일정 조회 요청: ${year}년 ${month}월, 사용자: ${targetEmployeeId}`);

      const response = await axios.get(`${API_BASE_URL}/monthly`, {
        params: {
          year: year,
          month: month
        },
        headers: {
          'X-Employee-Id': targetEmployeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('월별 일정 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('월별 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '월별 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 getMonthlySchedules가 employeeId를 받도록 변경되었으므로, 여기도 조정
  async getCalendarSchedules(currentDate, employeeId) {
    try {
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1;
      return await this.getMonthlySchedules(year, month, employeeId); // 🔧 employeeId 전달

    } catch (error) {
      console.error('달력 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '달력 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 employeeId 인자 추가
  async checkScheduleAccess(scheduleId, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`일정 접근 권한 확인 요청: 일정 ${scheduleId}, 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/${scheduleId}/access`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일정 접근 권한 확인 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 접근 권한 확인 실패:', error);
      const errorMessage = handleApiError(error, '일정 접근 권한 확인 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 employeeId 인자 추가
  async searchSchedules(keyword, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`일정 검색 요청: 키워드 "${keyword}", 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/search`, {
        params: {
          keyword: keyword
        },
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일정 검색 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 검색 실패:', error);
      const errorMessage = handleApiError(error, '일정 검색 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  async getEmployees(keyword) {
    try {
      console.log('직원 검색 요청:', keyword);

      const response = await axios.get(`${API_BASE_URL}/employees/search`, {
        params: {
          keyword: keyword
        }
      });

      console.log('직원 검색 성공:', response.data);
      return response.data;

    } catch (error) {
      console.error('직원 검색 실패:', error);

      console.warn('백엔드 API 실패, 목업 데이터 사용');
      // 🔧 목업 데이터 사용 시나리오를 위한 임시 반환
      return [
        { employeeId: 'EMP001', name: '김철수', department: '개발팀', position: '주임' },
        { employeeId: 'EMP002', name: '이영희', department: '영업팀', position: '대리' },
        { employeeId: 'EMP003', name: '박민수', department: '인사팀', position: '과장' },
      ].filter(emp => emp.name.includes(keyword) || emp.employeeId.includes(keyword));
    }
  },


  // 🔧 employeeId 인자 추가
  async getSchedules(employeeId = null) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거
      const targetEmployeeId = employeeId; // 🔧 인자로 받은 employeeId 사용

      console.log(`일정 목록 조회 요청: 사용자 ${targetEmployeeId}`);

      const response = await axios.get(API_BASE_URL, {
        headers: {
          'X-Employee-Id': targetEmployeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일정 목록 조회 성공:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 목록 조회 실패:', error);
      const errorMessage = handleApiError(error, '일정 조회에 실패했습니다.');
      throw new Error(errorMessage);
    }
  },


  // 🔧 employeeId 인자 추가
  async getScheduleById(scheduleId, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`일정 상세 조회 요청: 일정 ID ${scheduleId}, 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/${scheduleId}`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일정 상세 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 상세 조회 실패:', error);
      const errorMessage = handleApiError(error, '일정 상세 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  async getEmployeesByDepartment(deptId) {
    try {
      console.log(`부서별 직원 조회 요청: 부서 ID ${deptId}`);

      const response = await axios.get(`${API_BASE_URL}/employees/department/${deptId}`);

      console.log('부서별 직원 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('부서별 직원 조회 실패:', error);
      const errorMessage = handleApiError(error, '부서별 직원 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 employeeId 인자 추가
  async getDailySchedules(date, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      const dateStr = date instanceof Date
        ? date.toISOString().split('T')[0]
        : date;

      console.log(`일별 일정 조회 요청: 날짜 ${dateStr}, 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/daily`, {
        params: { date: dateStr },
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일별 일정 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('일별 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '일별 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },


  // 🔧 employeeId 인자 추가
  async getTodaySchedules(employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`오늘 일정 조회 요청: 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/today`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('오늘 일정 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('오늘 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '오늘 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 employeeId 인자 추가
  async getUpcomingSchedules(days = 7, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`다가오는 일정 조회 요청: ${days}일 이내, 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/upcoming`, {
        params: { days: days },
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('다가오는 일정 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('다가오는 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '다가오는 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 employeeId 인자 추가
  async getScheduleStatistics(employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`일정 통계 조회 요청: 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/statistics`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일정 통계 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 통계 조회 실패:', error);
      const errorMessage = handleApiError(error, '일정 통계 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },


  // 🔧 employeeId 인자 추가
  async getParticipantList(scheduleId, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`일정 참여자 조회 요청: 일정 ID ${scheduleId}, 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/${scheduleId}/participants`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('일정 참여자 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 참여자 조회 실패:', error);
      const errorMessage = handleApiError(error, '일정 참여자 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },


  // 🔧 employeeId 인자 추가 (현재 로그인한 사용자 ID)
  async getSchedulesByEmployee(targetEmployeeId, employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`직원별 일정 조회 요청: 대상 직원 ${targetEmployeeId}, 요청자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/employee/${targetEmployeeId}`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('직원별 일정 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('직원별 일정 조회 실패:', error);

      if (error.response?.status === 403) {
        throw new Error('다른 직원의 일정을 조회할 권한이 없습니다.');
      }

      const errorMessage = handleApiError(error, '직원별 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },


  // 🔧 employeeId 인자 추가
  async getOngoingSchedules(employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`진행 중인 일정 조회 요청: 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/ongoing`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('진행 중인 일정 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('진행 중인 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '진행 중인 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  // 🔧 employeeId 인자 추가
  async getThisWeekSchedules(employeeId) {
    try {
      // const currentUser = getCurrentUser(); // 🔧 제거

      console.log(`이번 주 일정 조회 요청: 사용자 ${employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/this-week`, {
        headers: {
          'X-Employee-Id': employeeId // 🔧 인자로 받은 employeeId 사용
        }
      });

      console.log('이번 주 일정 조회 완료:', response.data);
      return response.data;

    } catch (error) {
      console.error('이번 주 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '이번 주 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  async checkHealth() {
    try {
      console.log('일정 서비스 상태 확인 요청');

      const response = await axios.get(`${API_BASE_URL}/health`);

      console.log('일정 서비스 상태:', response.data);
      return response.data;

    } catch (error) {
      console.error('서비스 상태 확인 실패:', error);
      return {
        status: 'DOWN',
        error: error.message,
        timestamp: new Date().toISOString()
      };
    }
  },

  async getApiInfo() {
    try {
      console.log('일정 API 정보 조회 요청');

      const response = await axios.get(`${API_BASE_URL}/info`);

      console.log('일정 API 정보:', response.data);
      return response.data;

    } catch (error) {
      console.error('API 정보 조회 실패:', error);
      const errorMessage = handleApiError(error, 'API 정보 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  isScheduleOngoing(schedule) {
    if (!schedule || !schedule.startDate || !schedule.endDate) {
      return false;
    }

    const now = new Date();
    const start = new Date(schedule.startDate);
    const end = new Date(schedule.endDate);

    return now >= start && now <= end;
  },


  isScheduleOnDate(schedule, date) {
    if (!schedule || !schedule.startDate || !schedule.endDate || !date) {
      return false;
    }

    const start = new Date(schedule.startDate);
    const end = new Date(schedule.endDate);
    const targetDate = new Date(date);

    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    targetDate.setHours(12, 0, 0, 0);

    return targetDate >= start && targetDate <= end;
  },

  formatScheduleTime(dateTimeStr) {
    if (!dateTimeStr) return '';

    const date = new Date(dateTimeStr);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');

    return `${hours}:${minutes}`;
  },

  formatScheduleDate(dateTimeStr) {
    if (!dateTimeStr) return '';

    const date = new Date(dateTimeStr);
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');

    return `${year}-${month}-${day}`;
  },

  getVisibilityText(visibility) {
    const visibilityMap = {
      'PRIVATE': '비공개',
      'PUBLIC': '전체공개',
      'GROUP': '부서공개',
      'DEPARTMENT': '부서공개'
    };

    return visibilityMap[visibility] || visibility;
  }
};

export default scheduleApiService;
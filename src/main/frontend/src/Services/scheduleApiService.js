// src/main/frontend/src/Services/scheduleApiService.js
import axios from 'axios';

const API_BASE_URL = '/api/schedules';

// API 에러 처리 유틸리티
const handleApiError = (error, defaultMessage = 'API 호출 중 오류가 발생했습니다.') => {
  console.error('API Error:', error);
  
  if (error.response) {
    // 서버에서 응답을 받았지만 에러 상태코드
    const { status, data } = error.response;
    
    if (status === 400) {
      // 유효성 검사 실패 등
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
    // 요청이 전송되었지만 응답을 받지 못함
    return '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.';
  } else {
    // 요청 설정 중 오류 발생
    return error.message || defaultMessage;
  }
};

// 현재 로그인한 사용자 정보 (실제로는 인증 시스템에서 가져와야 함)
const getCurrentUser = () => {
  // TODO: 실제 환경에서는 JWT 토큰이나 세션에서 사용자 정보 추출
  return {
    employeeId: 'TEMP_USER_001',
    name: '홍길동',
    deptId: 'DEPT_001'
  };
};

// 날짜 형식 변환 유틸리티
const formatDateTimeForBackend = (dateTimeString) => {
  if (!dateTimeString) return null;
  
  // datetime-local 값이 'YYYY-MM-DDTHH:mm' 형태라면 ':00'을 추가
  if (dateTimeString.length === 16 && dateTimeString.includes('T')) {
    const formatted = dateTimeString + ':00';
    console.log(`날짜 형식 변환: ${dateTimeString} → ${formatted}`);
    return formatted;
  }
  
  return dateTimeString;
};

// 🔧 수정: 데이터 유효성 검사 및 기본값 설정 함수
const validateAndNormalizeScheduleData = (scheduleData) => {
  // 필수 필드 검사 및 기본값 설정
  const normalized = {
    title: scheduleData.title || '',
    description: scheduleData.description || '',
    startDate: scheduleData.startDate || '',
    endDate: scheduleData.endDate || '',
    visibility: scheduleData.visibility || 'PUBLIC', // 🔧 기본값 설정
    isAlarmEnabled: Boolean(scheduleData.isAlarmEnabled),
    alarmTime: scheduleData.alarmTime || null,
    selectedParticipants: Array.isArray(scheduleData.selectedParticipants) 
      ? scheduleData.selectedParticipants 
      : []
  };

  console.log('데이터 정규화 결과:', normalized);
  return normalized;
};

// 일정 관리 API 서비스
const scheduleApiService = {
  
 /**
   * 일정 등록
   * @param {Object} scheduleData - 일정 등록 데이터
   * @returns {Promise} API 응답
   */
  async registerSchedule(scheduleData) {
    try {
      const currentUser = getCurrentUser();
      
      // 🔧 수정: 데이터 유효성 검사 및 정규화
      const normalizedData = validateAndNormalizeScheduleData(scheduleData);
      console.log('정규화된 일정 데이터:', normalizedData);

      // 🔧 수정: null/undefined 방어 코드 추가
      const originalVisibility = normalizedData.visibility;
      let backendVisibility;
      
      if (!originalVisibility || typeof originalVisibility !== 'string') {
        console.warn('유효하지 않은 visibility 값, 기본값 PUBLIC 사용:', originalVisibility);
        backendVisibility = 'PUBLIC';
      } else {
        // 공개범위 변환 처리 (안전하게)
        backendVisibility = originalVisibility === 'DEPARTMENT' ? 'GROUP' : originalVisibility.toUpperCase();
        if (originalVisibility !== backendVisibility) {
          console.log(`공개범위 변환: ${originalVisibility} → ${backendVisibility}`);
        }
      }

      // 🔧 수정: 백엔드 DTO 필드명에 맞게 데이터 변환
      const requestDto = {
        title: normalizedData.title,
        description: normalizedData.description,
        // ✅ startDate, endDate로 변경
        startDate: formatDateTimeForBackend(normalizedData.startDate),
        endDate: formatDateTimeForBackend(normalizedData.endDate),
        visibility: backendVisibility,
        alarmEnabled: normalizedData.isAlarmEnabled,
        alarmTime: normalizedData.isAlarmEnabled 
          ? formatDateTimeForBackend(normalizedData.alarmTime) 
          : null,
        participantIds: normalizedData.selectedParticipants?.map(p => p.employeeId) || []
      };

      console.log('일정 등록 요청 데이터:', requestDto);

      const response = await axios.post(API_BASE_URL, requestDto, {
        headers: {
          'Content-Type': 'application/json',
          'X-Employee-Id': currentUser.employeeId
        }
      });

      console.log('일정 등록 성공:', response.data);
      return response.data;

    } catch (error) {
      console.error('일정 등록 실패:', error);
      
      // 유효성 검사 에러 처리
      if (error.response?.status === 400 && error.response?.data?.errors) {
        const validationErrors = error.response.data.errors;
        console.log('유효성 검사 에러:', validationErrors);
        
        // ValidationError 배열을 사용자 친화적 메시지로 변환
        const errorMessages = validationErrors.map(err => 
          `${getFieldDisplayName(err.field)}: ${err.message}`
        ).join('\n');
        
        throw new Error(errorMessages);
      }
      
      const errorMessage = handleApiError(error, '일정 등록에 실패했습니다.');
      throw new Error(errorMessage);
    }
  },

  /**
   * 월별 일정 조회 (달력 최적화용)
   * @param {number} year - 년도
   * @param {number} month - 월 (1-12)
   * @param {string} employeeId - 직원 ID (선택사항)
   * @returns {Promise} 해당 월의 일정 목록
   */
  async getMonthlySchedules(year, month, employeeId = null) {
    try {
      const currentUser = getCurrentUser();
      const targetEmployeeId = employeeId || currentUser.employeeId;

      console.log(`월별 일정 조회 요청: ${year}년 ${month}월, 사용자: ${targetEmployeeId}`);

      const response = await axios.get(`${API_BASE_URL}/monthly`, {
        params: {
          year: year,
          month: month
        },
        headers: {
          'X-Employee-Id': currentUser.employeeId
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

  /**
   * 달력 화면용 최적화된 일정 조회
   * 현재 월과 이전/다음 월의 일부 날짜까지 포함
   * @param {Date} currentDate - 현재 표시 중인 날짜
   * @returns {Promise} 달력 표시용 일정 목록
   */
  async getCalendarSchedules(currentDate) {
    try {
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1; // JavaScript의 월은 0부터 시작

      // 현재 월의 일정을 가져와서 달력에 표시
      return await this.getMonthlySchedules(year, month);

    } catch (error) {
      console.error('달력 일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '달력 일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  /**
   * 특정 일정 접근 권한 확인
   * @param {string} scheduleId - 일정 ID
   * @returns {Promise<boolean>} 접근 권한 여부
   */
  async checkScheduleAccess(scheduleId) {
    try {
      const currentUser = getCurrentUser();

      console.log(`일정 접근 권한 확인 요청: 일정 ${scheduleId}, 사용자 ${currentUser.employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/${scheduleId}/access`, {
        headers: {
          'X-Employee-Id': currentUser.employeeId
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

  /**
   * 일정 검색 (키워드 기반)
   * @param {string} keyword - 검색 키워드
   * @returns {Promise} 검색된 일정 목록
   */
  async searchSchedules(keyword) {
    try {
      const currentUser = getCurrentUser();

      console.log(`일정 검색 요청: 키워드 "${keyword}", 사용자 ${currentUser.employeeId}`);

      const response = await axios.get(`${API_BASE_URL}/search`, {
        params: {
          keyword: keyword
        },
        headers: {
          'X-Employee-Id': currentUser.employeeId
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

  /**
   * 직원 검색
   * @param {string} keyword - 검색 키워드
   * @returns {Promise<Array>} 직원 목록
   */
  async getEmployees(keyword) {
    try {
      // TODO: 실제 직원 검색 API 구현
      const response = await axios.get(`/api/employees/search`, {
        params: { keyword }
      });
      return response.data;
    } catch (error) {
      console.error('직원 검색 실패:', error);
      
      // 임시 목업 데이터 반환 (개발용)
      const mockEmployees = [
        { employeeId: 'EMP001', name: '홍길동', deptName: '개발팀'},
        { employeeId: 'EMP002', name: '김철수', deptName: '개발팀'},
        { employeeId: 'EMP003', name: '이영희', deptName: '기획팀'},
        { employeeId: 'EMP004', name: '박미영', deptName: '디자인팀'},
        { employeeId: 'EMP005', name: '정수호', deptName: '개발팀' },
        { employeeId: 'EMP006', name: '최지영', deptName: '마케팅팀' }
      ].filter(emp => emp.name.includes(keyword));
      
      return mockEmployees;
    }
  },

 /**
   * 일정 목록 조회
   * @param {string} employeeId - 직원 ID
   * @returns {Promise<Array>} 일정 목록
   */
  async getSchedules(employeeId) {
    try {
      const response = await axios.get(API_BASE_URL, {
        params: { employeeId }
      });
      return response.data;
    } catch (error) {
      console.error('일정 조회 실패:', error);
      const errorMessage = handleApiError(error, '일정 조회에 실패했습니다.');
      throw new Error(errorMessage);
    }
  },

  /**
   * 특정 일정 상세 조회
   * @param {string} scheduleId - 일정 ID
   * @returns {Promise<Object>} 일정 상세 정보 (참여자 정보 포함)
   */
  async getScheduleById(scheduleId) {
    try {
      console.log('일정 상세 조회 요청:', scheduleId);
      const response = await axios.get(`${API_BASE_URL}/${scheduleId}`);
      console.log('일정 상세 조회 응답:', response.data);
      return response.data;
    } catch (error) {
      console.error('일정 상세 조회 실패:', error);
      const errorMessage = handleApiError(error, '일정 조회에 실패했습니다.');
      throw new Error(errorMessage);
    }
  }
};

/**
 * 필드명을 사용자 친화적 이름으로 변환
 * @param {string} fieldName - 필드명
 * @returns {string} 표시용 필드명
 */
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

export default scheduleApiService;
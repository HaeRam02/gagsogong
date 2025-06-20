// scheduleApiService.js - 일정 관리 API 호출 서비스
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
      
      // 공개범위 변환 처리
      const originalVisibility = scheduleData.visibility;
      const backendVisibility = originalVisibility === 'DEPARTMENT' ? 'GROUP' : originalVisibility.toUpperCase();
      if (originalVisibility !== backendVisibility) {
        console.log(`공개범위 변환: ${originalVisibility} → ${backendVisibility}`);
      }

      // 백엔드 ScheduleRegisterRequestDTO에 맞게 데이터 변환
      const requestDto = {
        title: scheduleData.title,
        description: scheduleData.description,
        startDateTime: formatDateTimeForBackend(scheduleData.startDate), // 필드명 수정
        endDateTime: formatDateTimeForBackend(scheduleData.endDate),     // 필드명 수정
        visibility: backendVisibility, // 변환된 공개범위 사용
        alarmEnabled: scheduleData.isAlarmEnabled,          // 필드명 수정
        alarmTime: scheduleData.isAlarmEnabled ? formatDateTimeForBackend(scheduleData.alarmTime) : null,
        participantIds: scheduleData.participants?.map(p => p.employeeId) || [],
        employeeId: currentUser.employeeId // 작성자 ID 추가
      };

      console.log('일정 등록 요청 데이터:', requestDto);
      console.log('보내는 날짜 형식:', {
        startDateTime: requestDto.startDateTime,
        endDateTime: requestDto.endDateTime,
        alarmTime: requestDto.alarmTime
      });

      const response = await axios.post(API_BASE_URL, requestDto, {
        headers: {
          'Content-Type': 'application/json',
          'X-Employee-Id': currentUser.employeeId // 임시 헤더
        }
      });

      console.log('일정 등록 응답:', response.data);
      return response.data;

    } catch (error) {
      const errorMessage = handleApiError(error, '일정 등록 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  /**
   * 일정 목록 조회 
   * @param {string} employeeId - 직원 ID (선택사항)
   * @returns {Promise} 일정 목록
   */
  async getSchedules(employeeId = null) {
    try {
      const currentUser = getCurrentUser();
      // eslint-disable-next-line no-unused-vars
      const targetEmployeeId = employeeId || currentUser.employeeId;

      // 백엔드에 해당 API가 구현되면 주석 해제
      // const response = await axios.get(`${API_BASE_URL}?employeeId=${targetEmployeeId}`, {
      //   headers: {
      //     'X-Employee-Id': currentUser.employeeId
      //   }
      // });
      // return response.data;

      // 임시로 더미 데이터 반환 (백엔드 API 구현 전까지)
      console.log('일정 목록 조회 요청 (더미 데이터 반환)');
      
      // 실제 서버 응답 시뮬레이션
      await new Promise(resolve => setTimeout(resolve, 500));
      
      return [
        {
          scheduleId: 'SCH001',
          title: '프로젝트 킥오프 미팅',
          description: '새 프로젝트 시작을 위한 킥오프 미팅입니다.',
          startDate: '2025-06-25T10:00:00',
          endDate: '2025-06-25T12:00:00',
          visibility: 'PUBLIC',
          isAlarmEnabled: true,
          alarmTime: '2025-06-25T09:30:00',
          participants: ['홍길동', '김철수', '이영희'],
          createdBy: '홍길동',
          createdAt: '2025-06-20T14:30:00'
        },
        {
          scheduleId: 'SCH002', 
          title: '월간 보고서 검토',
          description: '6월 월간 보고서 최종 검토 및 승인',
          startDate: '2025-06-30T14:00:00',
          endDate: '2025-06-30T16:00:00',
          visibility: 'GROUP',
          isAlarmEnabled: false,
          participants: ['김철수', '박미영'],
          createdBy: '김철수',
          createdAt: '2025-06-20T15:00:00'
        }
      ];

    } catch (error) {
      const errorMessage = handleApiError(error, '일정 목록 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  /**
   * 개별 일정 조회
   * @param {string} scheduleId - 일정 ID
   * @returns {Promise} 일정 상세 정보
   */
  async getScheduleById(scheduleId) {
    try {
      const currentUser = getCurrentUser();
      // eslint-disable-next-line no-unused-vars
      const userEmployeeId = currentUser.employeeId;

      // 백엔드에 해당 API가 구현되면 주석 해제
      // const response = await axios.get(`${API_BASE_URL}/${scheduleId}`, {
      //   headers: {
      //     'X-Employee-Id': userEmployeeId
      //   }
      // });
      // return response.data;

      // 임시로 더미 데이터 반환 (백엔드 API 구현 전까지)
      console.log(`일정 상세 조회 요청: ${scheduleId} (더미 데이터 반환)`);
      
      await new Promise(resolve => setTimeout(resolve, 300));
      
      // 일정 목록에서 해당 일정 찾기 (임시)
      const schedules = await this.getSchedules();
      const schedule = schedules.find(s => s.scheduleId === scheduleId);
      
      if (!schedule) {
        throw new Error('일정을 찾을 수 없습니다.');
      }
      
      return schedule;

    } catch (error) {
      const errorMessage = handleApiError(error, '일정 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  /**
   * 직원 목록 조회 (참여자 선택용)
   * @param {string} keyword - 검색 키워드 (선택사항)
   * @returns {Promise} 직원 목록
   */
  async getEmployees(keyword = '') {
    try {
      // 실제로는 직원 관리 API 호출
      // const response = await axios.get('/api/employees', {
      //   params: { keyword }
      // });
      // return response.data;

      // 임시로 더미 데이터 반환
      console.log(`직원 목록 조회 요청 (키워드: ${keyword})`);
      
      await new Promise(resolve => setTimeout(resolve, 200));
      
      const allEmployees = [
        { employeeId: 'EMP001', name: '홍길동', department: '개발팀', position: '팀장' },
        { employeeId: 'EMP002', name: '김철수', department: '개발팀', position: '선임개발자' },
        { employeeId: 'EMP003', name: '이영희', department: '기획팀', position: '기획자' },
        { employeeId: 'EMP004', name: '박미영', department: '디자인팀', position: '디자이너' },
        { employeeId: 'EMP005', name: '정수호', department: '개발팀', position: '개발자' },
        { employeeId: 'EMP006', name: '최지영', department: '마케팅팀', position: '마케터' }
      ];

      if (!keyword.trim()) {
        return allEmployees;
      }

      return allEmployees.filter(employee => 
        employee.name.toLowerCase().includes(keyword.toLowerCase()) ||
        employee.department.toLowerCase().includes(keyword.toLowerCase()) ||
        employee.position.toLowerCase().includes(keyword.toLowerCase())
      );

    } catch (error) {
      const errorMessage = handleApiError(error, '직원 목록 조회 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  },

  /**
   * 알람 등록 (일정과 연동)
   * @param {Object} alarmData - 알람 데이터
   * @returns {Promise} 알람 등록 응답
   */
  async scheduleAlarm(alarmData) {
    try {
      // 알람 관리 API 호출
      const response = await axios.post('/api/alarms', {
        targetId: alarmData.scheduleId,
        domainType: 'SCHEDULE',
        noticeTime: formatDateTimeForBackend(alarmData.alarmTime),
        title: `일정 알림: ${alarmData.scheduleTitle}`,
        content: alarmData.scheduleDescription || '등록된 일정이 곧 시작됩니다.'
      });

      console.log('알람 등록 응답:', response.data);
      return response.data;

    } catch (error) {
      const errorMessage = handleApiError(error, '알람 등록 중 오류가 발생했습니다.');
      throw new Error(errorMessage);
    }
  }
};

export default scheduleApiService;
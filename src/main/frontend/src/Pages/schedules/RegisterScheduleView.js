// src/main/frontend/src/Pages/schedules/RegisterScheduleView.js 
// (폼 데이터 및 제출 핸들러 수정 부분)

import React, { useState, useEffect } from 'react';
import scheduleApiService from '../../Services/scheduleApiService';
import './RegisterScheduleView.css';

const RegisterScheduleView = ({ onBack, onSubmit }) => {
  // 🔧 수정: 일정 등록 폼 상태 (SDD의 ScheduleRegisterRequestDTO 기반) - 기본값 명시적 설정
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    visibility: 'PUBLIC', // 🔧 명시적 기본값
    isAlarmEnabled: false,
    alarmTime: ''
  });

  // 참여자 관련 상태
  const [selectedParticipants, setSelectedParticipants] = useState([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [showEmployeeSearch, setShowEmployeeSearch] = useState(false);

  // 유효성 검사 에러
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [employeeSearchLoading, setEmployeeSearchLoading] = useState(false);

  // 🔧 수정: 폼 데이터 변경 핸들러 - 로깅 추가
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    console.log(`handleInputChange: ${name} = ${type === 'checkbox' ? checked : value}`);
    
    setFormData(prev => {
      const newData = {
        ...prev,
        [name]: type === 'checkbox' ? checked : value
      };
      
      console.log('폼 데이터 변경:', newData);
      
      // 알람이 활성화될 때 기본 알람 시간 설정
      if (name === 'isAlarmEnabled' && checked && !prev.alarmTime) {
        if (prev.startDate) {
          // 시작 시간 30분 전을 알람 시간으로 설정
          const startTime = new Date(prev.startDate);
          const alarmTime = new Date(startTime.getTime() - 30 * 60 * 1000);
          const now = new Date();
          
          // 알람 시간이 현재 시간 이후가 되도록 조정
          if (alarmTime > now) {
            newData.alarmTime = alarmTime.toISOString().slice(0, 16);
          } else {
            // 현재 시간 5분 후를 알람 시간으로
            const futureAlarm = new Date(now.getTime() + 5 * 60 * 1000);
            newData.alarmTime = futureAlarm.toISOString().slice(0, 16);
          }
        }
      }
      
      // 시작 시간이 변경될 때 알람 시간도 자동 조정
      if (name === 'startDate' && prev.isAlarmEnabled) {
        const startTime = new Date(value);
        const alarmTime = new Date(startTime.getTime() - 30 * 60 * 1000);
        const now = new Date();
        
        if (alarmTime > now) {
          newData.alarmTime = alarmTime.toISOString().slice(0, 16);
        }
      }
      
      return newData;
    });

    // 에러 클리어
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // 최소 선택 가능한 시간 (현재 시간)
  const getMinDateTime = () => {
    const now = new Date();
    return now.toISOString().slice(0, 16);
  };

  // 직원 검색 함수 - API 기반
  const searchEmployees = async (keyword) => {
    if (!keyword.trim()) {
      setSearchResults([]);
      return;
    }

    setEmployeeSearchLoading(true);
    try {
      const results = await scheduleApiService.getEmployees(keyword);
      setSearchResults(results);
    } catch (error) {
      console.error('직원 검색 실패:', error);
      setSearchResults([]);
    } finally {
      setEmployeeSearchLoading(false);
    }
  };

  // 검색어 변경 핸들러 - 디바운싱 적용
  const handleSearchKeywordChange = (e) => {
    const keyword = e.target.value;
    setSearchKeyword(keyword);
    
    // 디바운싱: 300ms 후에 검색 실행
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    
    searchTimeoutRef.current = setTimeout(() => {
      searchEmployees(keyword);
    }, 300);
  };

  // 검색 타임아웃 참조
  const searchTimeoutRef = React.useRef(null);

  // 참여자 추가
  const addParticipant = (employee) => {
    const isAlreadySelected = selectedParticipants.some(
      p => p.employeeId === employee.employeeId
    );

    if (!isAlreadySelected) {
      setSelectedParticipants(prev => [...prev, employee]);
    }
    
    setSearchKeyword('');
    setSearchResults([]);
    setShowEmployeeSearch(false);
  };

  // 참여자 제거
  const removeParticipant = (employeeId) => {
    setSelectedParticipants(prev => 
      prev.filter(p => p.employeeId !== employeeId)
    );
  };

  // 유효성 검사 (SDD의 ScheduleValidator 기반)
  const validateForm = () => {
    const newErrors = {};
    const now = new Date();

    // 제목 검사
    if (!formData.title.trim()) {
      newErrors.title = '일정 제목을 입력해주세요.';
    } else if (formData.title.length > 100) {
      newErrors.title = '제목은 100자 이내로 입력해주세요.';
    }

    // 시작일 검사
    if (!formData.startDate) {
      newErrors.startDate = '시작일시를 선택해주세요.';
    } else {
      const startDateTime = new Date(formData.startDate);
      if (startDateTime <= now) {
        newErrors.startDate = '시작일시는 현재 시간 이후여야 합니다.';
      }
    }

    // 종료일 검사
    if (!formData.endDate) {
      newErrors.endDate = '종료일시를 선택해주세요.';
    } else {
      const endDateTime = new Date(formData.endDate);
      if (endDateTime <= now) {
        newErrors.endDate = '종료일시는 현재 시간 이후여야 합니다.';
      }
    }

    // 시작일과 종료일 비교
    if (formData.startDate && formData.endDate) {
      const startDateTime = new Date(formData.startDate);
      const endDateTime = new Date(formData.endDate);
      
      if (startDateTime >= endDateTime) {
        newErrors.endDate = '종료일시는 시작일시보다 늦어야 합니다.';
      }
    }

    // 알람 시간 검사
    if (formData.isAlarmEnabled) {
      if (!formData.alarmTime) {
        newErrors.alarmTime = '알람을 사용하려면 알람 시간을 설정해주세요.';
      } else {
        const alarmDateTime = new Date(formData.alarmTime);
        
        // 알람 시간이 현재 시간 이후인지 검사
        if (alarmDateTime <= now) {
          newErrors.alarmTime = '알람 시간은 현재 시간 이후여야 합니다.';
        }
        
        // 알람 시간이 시작 시간보다 이전인지 검사
        if (formData.startDate) {
          const startDateTime = new Date(formData.startDate);
          if (alarmDateTime >= startDateTime) {
            newErrors.alarmTime = '알람 시간은 일정 시작 시간보다 이전이어야 합니다.';
          }
        }
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 🔧 수정: 폼 제출 - 데이터 검증 및 로깅 강화
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    console.log('handleSubmit 시작 - 현재 formData:', formData);
    console.log('handleSubmit 시작 - 현재 selectedParticipants:', selectedParticipants);
    
    if (!validateForm()) {
      console.log('유효성 검사 실패:', errors);
      return;
    }

    setIsSubmitting(true);

    try {
      // 🔧 수정: 전송할 데이터 구조 명시적 생성 및 검증
      const submitData = {
        title: formData.title,
        description: formData.description,
        startDate: formData.startDate,
        endDate: formData.endDate,
        visibility: formData.visibility || 'PUBLIC', // 기본값 보장
        isAlarmEnabled: formData.isAlarmEnabled,
        alarmTime: formData.alarmTime,
        selectedParticipants: selectedParticipants
      };

      console.log('전송할 데이터:', submitData);
      
      // 필수 필드 재검증
      if (!submitData.visibility) {
        console.error('visibility 필드가 누락됨');
        throw new Error('공개범위 설정에 오류가 있습니다.');
      }
      
      // 등록 성공 시 부모 컴포넌트에 알림
      if (onSubmit) {
        await onSubmit(submitData);
      }
      
    } catch (error) {
      console.error('일정 등록 실패:', error);
      alert(`일정 등록 실패: ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  // 컴포넌트 언마운트 시 타이머 정리
  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  // 🔧 수정: 현재 날짜/시간을 기본값으로 설정 - 로깅 추가
  useEffect(() => {
    const now = new Date();
    // 현재 시간에서 10분 후를 시작 시간으로 (서버 시간과의 차이 고려)
    const startTime = new Date(now.getTime() + 10 * 60 * 1000); 
    // 시작 시간에서 1시간 후를 종료 시간으로
    const endTime = new Date(startTime.getTime() + 60 * 60 * 1000);
    
    // datetime-local input에 맞는 형식 (YYYY-MM-DDTHH:mm)
    const formatForInput = (date) => {
      return date.toISOString().slice(0, 16);
    };
    
    const defaultStartDate = formatForInput(startTime);
    const defaultEndDate = formatForInput(endTime);
    
    console.log('기본 날짜/시간 설정:', { defaultStartDate, defaultEndDate });
    
    setFormData(prev => ({
      ...prev,
      startDate: defaultStartDate,
      endDate: defaultEndDate
    }));
  }, []);

  // 🔧 수정: 디버깅용 - formData 변화 모니터링
  useEffect(() => {
    console.log('formData 변화 감지:', formData);
  }, [formData]);

  return (
    <div className="register-schedule-container">
      <div className="register-header">
        <button className="back-btn" onClick={onBack}>
          ← 목록으로
        </button>
        <h2>새 일정 등록</h2>
      </div>

      <form className="schedule-form" onSubmit={handleSubmit}>
        {/* 기본 정보 섹션 */}
        <div className="form-section">
          <h3>기본 정보</h3>
          
          <div className="form-group">
            <label htmlFor="title">일정 제목 *</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              placeholder="일정 제목을 입력하세요"
              className={errors.title ? 'error' : ''}
            />
            {errors.title && <span className="error-message">{errors.title}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="description">일정 설명</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="일정에 대한 상세 설명을 입력하세요"
              rows={4}
            />
          </div>

          <div className="form-group">
            <label htmlFor="visibility">공개 범위 *</label>
            <select
              id="visibility"
              name="visibility"
              value={formData.visibility}
              onChange={handleInputChange}
              className={errors.visibility ? 'error' : ''}
            >
              <option value="PUBLIC">전체 공개</option>
              <option value="GROUP">그룹 공개</option>
              <option value="PRIVATE">비공개</option>
            </select>
            {errors.visibility && <span className="error-message">{errors.visibility}</span>}
            {/* 🔧 디버깅: 현재 선택된 값 표시 */}
            <small style={{color: '#666', fontSize: '12px'}}>
              현재 선택된 값: {formData.visibility}
            </small>
          </div>
        </div>

        {/* 일시 설정 섹션 */}
        <div className="form-section">
          <h3>일시 설정</h3>
          
          <div className="datetime-group">
            <div className="form-group">
              <label htmlFor="startDate">시작 일시 *</label>
              <input
                type="datetime-local"
                id="startDate"
                name="startDate"
                value={formData.startDate}
                onChange={handleInputChange}
                min={getMinDateTime()}
                className={errors.startDate ? 'error' : ''}
              />
              {errors.startDate && <span className="error-message">{errors.startDate}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="endDate">종료 일시 *</label>
              <input
                type="datetime-local"
                id="endDate"
                name="endDate"
                value={formData.endDate}
                onChange={handleInputChange}
                min={getMinDateTime()}
                className={errors.endDate ? 'error' : ''}
              />
              {errors.endDate && <span className="error-message">{errors.endDate}</span>}
            </div>
          </div>
        </div>

        {/* 알람 설정 섹션 */}
        <div className="form-section">
          <h3>알람 설정</h3>
          
          <div className="form-group checkbox-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="isAlarmEnabled"
                checked={formData.isAlarmEnabled}
                onChange={handleInputChange}
              />
              <span className="checkmark"></span>
              알람 사용
            </label>
          </div>

          {formData.isAlarmEnabled && (
            <div className="form-group">
              <label htmlFor="alarmTime">알람 시간</label>
              <input
                type="datetime-local"
                id="alarmTime"
                name="alarmTime"
                value={formData.alarmTime}
                onChange={handleInputChange}
                min={getMinDateTime()}
                className={errors.alarmTime ? 'error' : ''}
              />
              {errors.alarmTime && <span className="error-message">{errors.alarmTime}</span>}
            </div>
          )}
        </div>

        {/* 참여자 설정 및 제출 버튼 등은 기존과 동일하게 유지 */}
        
        {/* 제출 버튼 */}
        <div className="form-actions">
          <button 
            type="button" 
            className="btn-secondary" 
            onClick={onBack}
            disabled={isSubmitting}
          >
            취소
          </button>
          <button 
            type="submit" 
            className="btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? '등록 중...' : '일정 등록'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default RegisterScheduleView;
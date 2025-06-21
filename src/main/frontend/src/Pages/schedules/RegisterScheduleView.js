import React, { useState, useEffect } from 'react';
import scheduleApiService from '../../Services/scheduleApiService';
import './RegisterScheduleView.css';

const RegisterScheduleView = ({ onBack, onSubmit }) => {
  // 일정 등록 폼 상태 (SDD의 ScheduleRegisterRequestDTO 기반)
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    visibility: 'PUBLIC',
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

  // 폼 데이터 변경 핸들러
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    
    setFormData(prev => {
      const newData = {
        ...prev,
        [name]: type === 'checkbox' ? checked : value
      };
      
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
      // 사용자에게 에러 표시 (선택사항)
      // alert(`직원 검색 실패: ${error.message}`);
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

  // 폼 제출
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const submitData = {
        title: formData.title,
        description: formData.description,
        startDate: formData.startDate,     // 백엔드와 동일한 필드명 사용
        endDate: formData.endDate,         // 백엔드와 동일한 필드명 사용
        visibility: formData.visibility,
        alarmEnabled: formData.isAlarmEnabled,  // isAlarmEnabled → alarmEnabled
        alarmTime: formData.alarmTime,
        selectedParticipants: selectedParticipants.map(p => p.id || p)  // 참여자 ID 배열
      };
      if (onSubmit) {
        onSubmit({ submitData});
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

  // 현재 날짜/시간을 기본값으로 설정 (충분히 미래 시간으로)
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
    
    setFormData(prev => ({
      ...prev,
      startDate: formatForInput(startTime),
      endDate: formatForInput(endTime)
    }));
  }, []);

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
            <label htmlFor="visibility">공개 범위</label>
            <select
              id="visibility"
              name="visibility"
              value={formData.visibility}
              onChange={handleInputChange}
            >
              <option value="PUBLIC">전체 공개</option>
              <option value="GROUP">그룹 공개</option>
              <option value="PRIVATE">비공개</option>
            </select>
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

        {/* 참여자 설정 섹션 */}
        <div className="form-section">
          <h3>참여자 설정</h3>
          
          <div className="participant-search">
            <div className="search-input-container">
              <input
                type="text"
                placeholder="직원 이름, 부서, 직급으로 검색..."
                value={searchKeyword}
                onChange={handleSearchKeywordChange}
                onFocus={() => setShowEmployeeSearch(true)}
              />
              <button 
                type="button" 
                className="search-btn"
                onClick={() => searchEmployees(searchKeyword)}
              >
                검색
              </button>
            </div>

            {showEmployeeSearch && (
              <div className="search-results">
                {employeeSearchLoading ? (
                  <div className="search-loading">검색 중...</div>
                ) : searchResults.length > 0 ? (
                  searchResults.map(employee => (
                    <div
                      key={employee.employeeId}
                      className="search-result-item"
                      onClick={() => addParticipant(employee)}
                    >
                      <div className="employee-info">
                        <div className="employee-name">{employee.name}</div>
                        <div className="employee-details">
                          {employee.deptName}
                        </div>
                      </div>
                    </div>
                  ))
                ) : searchKeyword.trim() && !employeeSearchLoading ? (
                  <div className="no-search-results">검색 결과가 없습니다.</div>
                ) : null}
              </div>
            )}
          </div>

          <div className="selected-participants">
            <h4>선택된 참여자 ({selectedParticipants.length}명)</h4>
            {selectedParticipants.length === 0 ? (
              <p className="no-participants">선택된 참여자가 없습니다.</p>
            ) : (
              <div className="participants-list">
                {selectedParticipants.map(participant => (
                  <div key={participant.employeeId} className="participant-item">
                    <div className="participant-info">
                      <div className="participant-name">{participant.name}</div>
                      <div className="participant-details">
                        {participant.department} · {participant.position}
                      </div>
                    </div>
                    <button
                      type="button"
                      className="remove-btn"
                      onClick={() => removeParticipant(participant.employeeId)}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 제출 버튼 */}
        <div className="form-actions">
          <button
            type="button"
            className="cancel-btn"
            onClick={onBack}
            disabled={isSubmitting}
          >
            취소
          </button>
          <button
            type="submit"
            className="submit-btn"
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
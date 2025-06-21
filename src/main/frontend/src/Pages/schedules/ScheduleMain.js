import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import RegisterScheduleView from './RegisterScheduleView';
import scheduleApiService from '../../Services/scheduleApiService';
import ScheduleCalendarView from '../../Components/ScheduleCalendar';

import './ScheduleMain.css';

const ScheduleMain = () => {
  const navigate = useNavigate();
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentView, setCurrentView] = useState('list'); // 'list', 'create', 'detail'
  const [selectedSchedule, setSelectedSchedule] = useState(null);

  useEffect(() => {
    // 컴포넌트 마운트 시 일정 목록 로드
    loadSchedules();
  }, []);

  const loadSchedules = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await scheduleApiService.getSchedules();
      setSchedules(data || []); // 🔧 안전한 기본값 설정
    } catch (error) {
      console.error('일정 목록 로드 실패:', error);
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSchedule = () => {
    setCurrentView('create');
  };

  const handleViewSchedule = async (schedule) => {
    try {
      // 상세 정보가 필요한 경우 API 호출
      const detailSchedule = await scheduleApiService.getScheduleById(schedule.scheduleId);
      setSelectedSchedule(detailSchedule);
      setCurrentView('detail');
    } catch (error) {
      console.error('일정 상세 조회 실패:', error);
      setError(error.message);
      // 에러가 발생해도 기본 정보로 상세보기는 가능하게
      setSelectedSchedule(schedule);
      setCurrentView('detail');
    }
  };

  const handleBackToList = () => {
    setCurrentView('list');
    setSelectedSchedule(null);
    setError(null);
  };

  const handleScheduleSubmit = async (scheduleData) => {
    try {
      setLoading(true);
      
      // API를 통해 일정 등록
      const result = await scheduleApiService.registerSchedule(scheduleData);
      
      if (result.success !== false) { // 등록 성공
        // 목록 새로고침
        await loadSchedules();
        setCurrentView('list');
        alert('일정이 성공적으로 등록되었습니다!');
      } else {
        // 백엔드에서 유효성 검사 실패 등으로 실패한 경우
        throw new Error(result.message || '일정 등록에 실패했습니다.');
      }
      
    } catch (error) {
      console.error('일정 등록 실패:', error);
      alert(`일정 등록 실패: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // 🔧 안전한 날짜 포맷팅
  const formatDateTime = (dateTimeString) => {
    try {
      if (!dateTimeString) return '날짜 없음';
      const date = new Date(dateTimeString);
      if (isNaN(date.getTime())) return '잘못된 날짜';
      
      return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      console.error('날짜 포맷팅 오류:', error);
      return '날짜 오류';
    }
  };

  const getVisibilityText = (visibility) => {
    switch(visibility) {
      case 'PUBLIC': return '전체 공개';
      case 'GROUP': return '그룹 공개';
      case 'DEPARTMENT': return '부서 공개';
      case 'PRIVATE': return '비공개';
      default: return visibility || '알 수 없음';
    }
  };

  // 🔧 안전한 참여자 표시 함수
  const renderParticipants = (schedule) => {
    try {
      // participants가 배열인지 확인
      if (Array.isArray(schedule.participants)) {
        return schedule.participants.length > 0 
          ? schedule.participants.join(', ')
          : '참여자 없음';
      }
      
      // participants가 객체나 다른 형태인 경우
      if (schedule.participants && typeof schedule.participants === 'object') {
        return '참여자 정보 있음';
      }
      
      // participants가 없는 경우
      return '참여자 정보 없음';
    } catch (error) {
      console.error('참여자 렌더링 오류:', error);
      return '참여자 정보 오류';
    }
  };

  // 🔧 안전한 상세 참여자 렌더링
  const renderDetailParticipants = (schedule) => {
    try {
      if (!schedule || !schedule.participants) {
        return <span className="no-participants">참여자 정보 없음</span>;
      }

      if (Array.isArray(schedule.participants)) {
        if (schedule.participants.length === 0) {
          return <span className="no-participants">참여자 없음</span>;
        }
        
        return schedule.participants.map((participant, index) => (
          <span key={index} className="participant-tag">
            {participant}
          </span>
        ));
      }

      return <span className="participant-info">참여자 정보 있음</span>;
    } catch (error) {
      console.error('상세 참여자 렌더링 오류:', error);
      return <span className="participant-error">참여자 정보 오류</span>;
    }
  };

  // 일정 목록 뷰
  const renderScheduleList = () => (
    <div className="schedule-list-container">
      <div className="schedule-header">
        <h1>일정 관리</h1>
        <button 
          className="create-btn"
          onClick={handleCreateSchedule}
        >
          새 일정 등록
        </button>
      </div>

      {loading ? (
        <div className="loading">일정을 불러오는 중...</div>
      ) : error ? (
        <div className="error-state">
          <p>오류가 발생했습니다: {error}</p>
          <button 
            className="retry-btn"
            onClick={loadSchedules}
          >
            다시 시도
          </button>
        </div>
      ) : (
        <div className="schedule-list">
          <ScheduleCalendarView/>
          {schedules.length === 0 ? (
            <div className="empty-state">
              <p>등록된 일정이 없습니다.</p>
              <button 
                className="create-btn-secondary"
                onClick={handleCreateSchedule}
              >
                첫 번째 일정 만들기
              </button>
            </div>
          ) : (
            <div className="schedule-grid">
              {schedules.map((schedule) => (
                <div 
                  key={schedule.scheduleId || schedule.id} 
                  className="schedule-card"
                  onClick={() => handleViewSchedule(schedule)}
                >
                  <div className="schedule-card-header">
                    <h3 className="schedule-title">{schedule.title || '제목 없음'}</h3>
                    <span className={`visibility-badge ${(schedule.visibility || 'private').toLowerCase()}`}>
                      {getVisibilityText(schedule.visibility)}
                    </span>
                  </div>
                  
                  <p className="schedule-description">
                    {schedule.description || '설명 없음'}
                  </p>
                  
                  <div className="schedule-info">
                    <div className="schedule-time">
                      <strong>시작:</strong> {formatDateTime(schedule.startDate)}
                    </div>
                    <div className="schedule-time">
                      <strong>종료:</strong> {formatDateTime(schedule.endDate)}
                    </div>
                  </div>
                  
                  {/* 🔧 안전한 참여자 표시 */}
                  <div className="schedule-participants">
                    <strong>참여자:</strong> {renderParticipants(schedule)}
                  </div>
                  
                  {schedule.alarmEnabled && (
                    <div className="alarm-indicator">
                      🔔 알람 설정됨
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );

  // 일정 상세 뷰
  const renderScheduleDetail = () => (
    <div className="schedule-detail-container">
      <div className="detail-header">
        <button 
          className="back-btn"
          onClick={handleBackToList}
        >
          ← 목록으로
        </button>
        <h2>일정 상세</h2>
      </div>

      {selectedSchedule && (
        <div className="schedule-detail">
          <div className="detail-section">
            <h3>{selectedSchedule.title || '제목 없음'}</h3>
            <span className={`visibility-badge ${(selectedSchedule.visibility || 'private').toLowerCase()}`}>
              {getVisibilityText(selectedSchedule.visibility)}
            </span>
          </div>

          <div className="detail-section">
            <h4>설명</h4>
            <p>{selectedSchedule.description || '설명 없음'}</p>
          </div>

          <div className="detail-section">
            <h4>일시</h4>
            <div className="time-info">
              <p><strong>시작:</strong> {formatDateTime(selectedSchedule.startDate)}</p>
              <p><strong>종료:</strong> {formatDateTime(selectedSchedule.endDate)}</p>
            </div>
          </div>

          <div className="detail-section">
            <h4>참여자</h4>
            <div className="participants-list">
              {renderDetailParticipants(selectedSchedule)}
            </div>
          </div>

          {selectedSchedule.alarmEnabled && selectedSchedule.alarmTime && (
            <div className="detail-section">
              <h4>알람</h4>
              <p>🔔 {formatDateTime(selectedSchedule.alarmTime)}</p>
            </div>
          )}

          <div className="detail-section">
            <h4>작성자</h4>
            <p>{selectedSchedule.createdBy || selectedSchedule.employeeId || '작성자 정보 없음'}</p>
          </div>
        </div>
      )}
    </div>
  );

  // 일정 등록 뷰
  const renderCreateSchedule = () => (
    <RegisterScheduleView 
      onBack={handleBackToList}
      onSubmit={handleScheduleSubmit}
    />
  );

   const rendereCalendarSchedule = () => (
    <ScheduleCalendarView />
  );

  // 현재 뷰에 따라 렌더링
  switch(currentView) {
    case 'create':
      return renderCreateSchedule();
    case 'detail':
      return renderScheduleDetail();
    case 'list':
    default:
      return renderScheduleList();
  }
};

export default ScheduleMain;
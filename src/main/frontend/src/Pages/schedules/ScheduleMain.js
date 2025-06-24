import React, { useState, useEffect, useContext } from 'react'; // useContext 임포트
import { useNavigate } from 'react-router-dom';
import RegisterScheduleView from './RegisterScheduleView';
import scheduleApiService from '../../Services/scheduleApiService';
import ScheduleCalendarView from '../../Components/ScheduleCalendar';
import { UserContext } from '../../Context/UserContext'; // UserContext 임포트

import './ScheduleMain.css';

const ScheduleMain = () => {
  const navigate = useNavigate();
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentView, setCurrentView] = useState('list'); // 'list', 'create', 'detail'
  const [selectedSchedule, setSelectedSchedule] = useState(null);

  const { loggedInUser } = useContext(UserContext); // UserContext에서 로그인 정보 가져오기

  useEffect(() => {
    loadSchedules();
  }, []);

  const loadSchedules = async () => {
    setLoading(true);
    setError(null);

    try {
      // 🔧 loggedInUser.employeeId를 getSchedules에 전달
      const data = await scheduleApiService.getSchedules(loggedInUser.id);
      setSchedules(data || []);
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
      // 🔧 loggedInUser.employeeId를 getScheduleById에 전달
      const detailSchedule = await scheduleApiService.getScheduleById(schedule.scheduleId, loggedInUser.id);
      setSelectedSchedule(detailSchedule);
      setCurrentView('detail');
    } catch (error) {
      console.error('일정 상세 조회 실패:', error);
      setError(error.message);
      setSelectedSchedule(schedule);
      setCurrentView('detail');
    }
  };

  const handleBackToList = () => {
    setCurrentView('list');
    setSelectedSchedule(null);
    setError(null);
  };

  const handleScheduleSubmit = async ({ submitData }) => {
    try {
      setLoading(true);

      console.log('handleScheduleSubmit - 전달받은 원본 데이터:', submitData);
    console.log('handleScheduleSubmit - UserContext loggedInUser:', loggedInUser);
            console.log('handleScheduleSubmit - UserContext ID:', loggedInUser?.id);

      if (!submitData) {
        throw new Error('일정 데이터가 제공되지 않았습니다.');
      }

      if (!submitData.title || !submitData.title.trim()) {
        throw new Error('일정 제목은 필수입니다.');
      }

      if (!submitData.startDate || !submitData.endDate) {
        throw new Error('시작 날짜와 종료 날짜는 필수입니다.');
      }

      const normalizedScheduleData = {
        title: submitData.title,
        description: submitData.description || '',
        startDate: submitData.startDate,
        endDate: submitData.endDate,
        visibility: submitData.visibility || 'PUBLIC',
        isAlarmEnabled: Boolean(submitData.isAlarmEnabled),
        alarmTime: submitData.alarmTime || null,
        selectedParticipants: submitData.selectedParticipants || []
      };

      console.log('handleScheduleSubmit - 정규화된 데이터:', normalizedScheduleData);

      // 🔧 loggedInUser.employeeId를 registerSchedule에 전달
      const result = await scheduleApiService.registerSchedule(normalizedScheduleData, loggedInUser.id);

      console.log('handleScheduleSubmit - API 응답:', result);

      if (result.success !== false) {
        await loadSchedules();
        setCurrentView('list');
        alert('일정이 성공적으로 등록되었습니다!');
      } else {
        throw new Error(result.message || '일정 등록에 실패했습니다.');
      }

    } catch (error) {
      console.error('handleScheduleSubmit - 일정 등록 실패:', error);

      let errorMessage = '일정 등록 중 오류가 발생했습니다.';

      if (error.message) {
        errorMessage = error.message;
      }

      if (error.message && error.message.includes('visibility')) {
        errorMessage += '\n공개 범위 설정에 문제가 있습니다. 다시 시도해주세요.';
      }

      setError(errorMessage);
      alert(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateTime) => {
    if (!dateTime) return '-';

    try {
      const date = new Date(dateTime);
      if (isNaN(date.getTime())) return '-';

      return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      });
    } catch (error) {
      console.error('날짜 포맷 실패:', error);
      return '-';
    }
  };

  const getVisibilityText = (visibility) => {
    switch (visibility) {
      case 'PUBLIC': return '전체 공개';
      case 'GROUP':
      case 'DEPARTMENT': return '그룹 공개';
      case 'PRIVATE': return '비공개';
      default: return '알 수 없음';
    }
  };

  const renderParticipants = (schedule) => {
    console.log('🔍 renderParticipants 호출:', schedule.participants);

    if (!schedule.participants || schedule.participants.length === 0) {
      return '참여자 없음';
    }

    if (schedule.participants.length === 1) {
      return schedule.participants[0];
    }

    return `${schedule.participants[0]} 외 ${schedule.participants.length - 1}명`;
  };

  const renderDetailParticipants = (schedule) => {
    console.log('🔍 renderDetailParticipants 호출:', schedule.participants, schedule.participantIds);

    if (!schedule.participants || schedule.participants.length === 0) {
      return <div className="no-participants">참여자가 없습니다.</div>;
    }


    return (
      <div className="participants-grid">
        {schedule.participants.map((participantName, index) => {
          const participantId = schedule.participantIds?.[index] || `unknown-${index}`;

          return (
            <div key={participantId} className="participant-item">
              <span className="participant-name">
                {participantName}
              </span>
              <span className="participant-dept">
                ({participantId})
              </span>
            </div>
          );
        })}
      </div>
    );
  };

  const renderScheduleList = () => (
    <div className="schedule-main">
      <div className="schedule-header">
        <h1>일정 관리</h1>
        <div className="header-actions">
          <button
            className="btn-primary"
            onClick={handleCreateSchedule}
          >
            + 새 일정
          </button>
        </div>
      </div>

      {error && (
        <div className="error-message">
          {error}
          <button onClick={() => setError(null)}>×</button>
        </div>
      )}

      {loading ? (
        <div className="loading">일정을 불러오는 중...</div>
      ) : (
        <div className="schedule-content">
          {schedules.length === 0 ? (
            <div className="empty-state">
              <p>등록된 일정이 없습니다.</p>
              <button
                className="btn-primary"
                onClick={handleCreateSchedule}
              >
                첫 번째 일정 만들기
              </button>
            </div>
          ) : (
            <div className="schedule-list">
              <ScheduleCalendarView />
              {schedules.map((schedule) => (
                <div
                  key={schedule.scheduleId}
                  className="schedule-item"
                  onClick={() => handleViewSchedule(schedule)}
                >
                  <div className="schedule-item-header">
                    <h3>{schedule.title || '제목 없음'}</h3>
                    <span className={`visibility-badge ${(schedule.visibility || 'private').toLowerCase()}`}>
                      {getVisibilityText(schedule.visibility)}
                    </span>
                  </div>

                  <div className="schedule-item-content">
                    <p className="schedule-description">
                      {schedule.description || '설명 없음'}
                    </p>

                    <div className="schedule-time">
                      {formatDateTime(schedule.startDate)} ~ {formatDateTime(schedule.endDate)}
                    </div>
                  </div>

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

  const renderCreateSchedule = () => (
    <RegisterScheduleView
      onBack={handleBackToList}
      onSubmit={handleScheduleSubmit}
      currentUser={loggedInUser} // 🔧 currentUser 정보 전달
    />
  );

  const renderCalendarSchedule = () => (
    <ScheduleCalendarView />
  );

  switch (currentView) {
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
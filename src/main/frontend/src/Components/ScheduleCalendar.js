// src/main/frontend/src/Components/ScheduleCalendar.js
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Calendar, ChevronLeft, ChevronRight, Plus, Eye, Users, Lock } from 'lucide-react';
import scheduleApiService from '../Services/scheduleApiService';
import './ScheduleCalendar.css';

const ScheduleCalendar = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [schedules, setSchedules] = useState([]);
  const [selectedSchedule, setSelectedSchedule] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // 현재 월의 첫 번째와 마지막 날짜 계산
  const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
  const lastDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
  
  // 달력 표시를 위한 시작일 (이전 달의 마지막 주 포함)
  const calendarStart = new Date(firstDayOfMonth);
  calendarStart.setDate(calendarStart.getDate() - firstDayOfMonth.getDay());
  
  // 달력 표시를 위한 종료일 (다음 달의 첫 주 포함)
  const calendarEnd = new Date(lastDayOfMonth);
  calendarEnd.setDate(calendarEnd.getDate() + (6 - lastDayOfMonth.getDay()));

  // 일정 로드
  useEffect(() => {
    loadSchedules();
  }, [currentDate]);

  const loadSchedules = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1;
      
      // 실제 API 호출로 월별 일정 조회
      const data = await scheduleApiService.getMonthlySchedules(year, month);

      setSchedules(data);
      
    } catch (error) {
      console.error('일정 로드 실패:', error);
      setError(error.message);
      
      // 에러 발생 시 빈 배열로 설정
      setSchedules([]);
    } finally {
      setLoading(false);
    }
  };

  // 특정 날짜의 일정들 가져오기
 function getSchedulesForDate(schedules, targetDate) {

  if (!Array.isArray(schedules)) {
    return [];
  }
  return schedules.filter(schedule => {
    // 1) 원본 날짜/시간 문자열 꺼내기
    const rawStart = schedule.startDate;
    const rawEnd   = schedule.endDate; 
    if (!rawStart || !rawEnd) return false;

    // 2) Date 객체 생성
    const start = new Date(rawStart);
    const end   = new Date(rawEnd);
    if (isNaN(start.getTime()) || isNaN(end.getTime())) return false;

    // 3) 시간 정보 제거하고 '날짜'만 비교
    const startDay  = new Date(start.getFullYear(), start.getMonth(), start.getDate());
    const endDay    = new Date(end.getFullYear(),   end.getMonth(),   end.getDate());
    const targetDay = new Date(
      targetDate.getFullYear(),
      targetDate.getMonth(),
      targetDate.getDate()
    );

    // 4) 시작일부터 종료일까지 포함되는 경우만 true
    return startDay <= targetDay && targetDay <= endDay;
});
}

  // 달력 날짜 배열 생성
  const generateCalendarDays = () => {
    const days = [];
    const current = new Date(calendarStart);
    
    while (current <= calendarEnd) {
      days.push(new Date(current));
      current.setDate(current.getDate() + 1);
    }
    
    return days;
  };

  // 월 이동
  const navigateMonth = (direction) => {
    const newDate = new Date(currentDate);
    newDate.setMonth(newDate.getMonth() + direction);
    setCurrentDate(newDate);
  };

  // 일정 상세 조회
  const handleScheduleClick = async (schedule) => {
    try {
      // 접근 권한 확인
      const hasAccess = await scheduleApiService.checkScheduleAccess(schedule.scheduleId);
      
      if (hasAccess) {
        // 상세 정보 조회
        const detailedSchedule = await scheduleApiService.getScheduleById(schedule.scheduleId);
        setSelectedSchedule(detailedSchedule);
      } else {
        alert('이 일정에 접근할 권한이 없습니다.');
      }
    } catch (error) {
      console.error('일정 상세 조회 실패:', error);
      alert('일정을 불러오는 중 오류가 발생했습니다.');
    }
  };

  // 공개범위 아이콘
  const getVisibilityIcon = (visibility) => {
    const iconClass = `visibility-icon ${visibility.toLowerCase()}`;
    
    switch (visibility) {
      case 'PUBLIC':
        return <Eye className={iconClass} />;
      case 'GROUP':
        return <Users className={iconClass} />;
      case 'PRIVATE':
        return <Lock className={iconClass} />;
      default:
        return null;
    }
  };

  // 공개범위 CSS 클래스
  const getVisibilityClass = (visibility) => {
    return visibility.toLowerCase();
  };

  const formatDate = (date) => {
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long'
    });
  };

  const formatTime = (dateTimeStr) => {
    return new Date(dateTimeStr).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  };

  const formatDateTime = (dateTimeStr) => {
    const date = new Date(dateTimeStr);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    }) + ' ' + formatTime(dateTimeStr);
  };

  // 날짜 셀 클래스 계산
  const getDateCellClass = (date) => {
    const isCurrentMonth = date.getMonth() === currentDate.getMonth();
    const isToday = date.toDateString() === new Date().toDateString();
    
    let cellClass = 'calendar-cell';
    if (!isCurrentMonth) cellClass += ' other-month';
    if (isToday) cellClass += ' today';
    
    return cellClass;
  };

  // 날짜 숫자 클래스 계산
  const getDateNumberClass = (date) => {
    const isCurrentMonth = date.getMonth() === currentDate.getMonth();
    const isToday = date.toDateString() === new Date().toDateString();
    const dayOfWeek = date.getDay();
    
    let numberClass = 'date-number';
    if (!isCurrentMonth) {
      numberClass += ' other-month';
    } else if (isToday) {
      numberClass += ' today';
    } else if (dayOfWeek === 0) {
      numberClass += ' sunday';
    } else if (dayOfWeek === 6) {
      numberClass += ' saturday';
    } else {
      numberClass += ' default';
    }
    
    return numberClass;
  };

  // 요일 헤더 클래스 계산
  const getWeekdayClass = (index) => {
    if (index === 0) return 'weekday-cell weekday-sunday';
    if (index === 6) return 'weekday-cell weekday-saturday';
    return 'weekday-cell weekday-default';
  };

  // 새로고침 처리
  const handleRefresh = () => {
    loadSchedules();
  };

  return (
    <div className="schedule-calendar">
      
      {/* 달력 네비게이션 */}
      <div className="calendar-navigation">
        <button
          onClick={() => navigateMonth(-1)}
          className="nav-button"
          disabled={loading}
        >
          <ChevronLeft />
        </button>
        
        <h2 className="current-month">
          {formatDate(currentDate)}
        </h2>
        
        <button
          onClick={() => navigateMonth(1)}
          className="nav-button"
          disabled={loading}
        >
          <ChevronRight />
        </button>
      </div>

      {/* 에러 메시지 */}
      {error && (
        <div style={{
          padding: '16px',
          marginBottom: '16px',
          backgroundColor: '#fef2f2',
          border: '1px solid #fecaca',
          borderRadius: '8px',
          color: '#b91c1c'
        }}>
          <p>일정을 불러오는 중 오류가 발생했습니다: {error}</p>
          <button 
            onClick={handleRefresh}
            style={{
              marginTop: '8px',
              padding: '4px 8px',
              backgroundColor: '#dc2626',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            다시 시도
          </button>
        </div>
      )}

      {/* 로딩 상태 */}
      {loading && (
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p className="loading-text">일정을 불러오는 중...</p>
        </div>
      )}

      {/* 달력 */}
      {!loading && (
        <div className="calendar-container">
          {/* 요일 헤더 */}
          <div className="weekday-header">
            {['일', '월', '화', '수', '목', '금', '토'].map((day, index) => (
              <div key={day} className={getWeekdayClass(index)}>
                {day}
              </div>
            ))}
          </div>

          {/* 날짜 그리드 */}
          <div className="calendar-grid">
            {generateCalendarDays().map((date, index) => {
              const daySchedules = getSchedulesForDate(schedules, date);

              return (
                <div key={index} className={getDateCellClass(date)}>
                  {/* 날짜 */}
                  <div className="date-container">
                    <span className={getDateNumberClass(date)}>
                      {date.getDate()}
                    </span>
                  </div>

                  {/* 일정 목록 */}
                  <div className="schedule-list">
                    {daySchedules.map((schedule) => (
                      <div
                        key={schedule.scheduleId}
                        onClick={() => handleScheduleClick(schedule)}
                        className={`schedule-item ${getVisibilityClass(schedule.visibility)}`}
                      >
                        <div className="schedule-header">
                          {getVisibilityIcon(schedule.visibility)}
                          <span className="schedule-title">{schedule.title}</span>
                        </div>
                        <div className="schedule-time">
                          {formatTime(schedule.startDateTime)}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* 일정 상세 모달 */}
      {selectedSchedule && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <div className="modal-title-container">
                {getVisibilityIcon(selectedSchedule.visibility)}
                <h3 className="modal-title">
                  {selectedSchedule.title}
                </h3>
              </div>
              <button
                onClick={() => setSelectedSchedule(null)}
                className="modal-close-btn"
              >
                ✕
              </button>
            </div>

            <div className="modal-body">
              <div className="modal-field">
                <span className="modal-label">내용:</span>
                <p className="modal-value">{selectedSchedule.description || '내용이 없습니다.'}</p>
              </div>

              <div className="modal-field">
                <span className="modal-label">시작 시간:</span>
                <p className="modal-value">
                  {formatDateTime(selectedSchedule.startDateTime)}
                </p>
              </div>

              <div className="modal-field">
                <span className="modal-label">종료 시간:</span>
                <p className="modal-value">
                  {formatDateTime(selectedSchedule.endDateTime)}
                </p>
              </div>

              <div className="modal-field">
                <span className="modal-label">공개 범위:</span>
                <p className="modal-value">
                  {selectedSchedule.visibility === 'PUBLIC' ? '전체 공개' :
                   selectedSchedule.visibility === 'GROUP' ? '그룹 공개' : '비공개'}
                </p>
              </div>

              {selectedSchedule.participants && selectedSchedule.participants.length > 0 && (
                <div className="modal-field">
                  <span className="modal-label">참여자:</span>
                  <p className="modal-value">
                    {selectedSchedule.participants.join(', ')}
                  </p>
                </div>
              )}

              {selectedSchedule.employeeId && (
                <div className="modal-field">
                  <span className="modal-label">작성자:</span>
                  <p className="modal-value">{selectedSchedule.employeeId}</p>
                </div>
              )}

              {selectedSchedule.alarmEnabled && (
                <div className="modal-field">
                  <span className="modal-label">알림:</span>
                  <p className="modal-value">
                    {formatDateTime(selectedSchedule.alarmTime)}에 알림 예정
                  </p>
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button
                onClick={() => setSelectedSchedule(null)}
                className="modal-btn secondary"
              >
                닫기
              </button>
              <button 
                className="modal-btn primary"
                onClick={() => alert('수정 기능은 추후 구현 예정입니다.')}
              >
                수정
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ScheduleCalendar;
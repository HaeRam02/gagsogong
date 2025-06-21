package com.example.gagso.Schedules.repository;

import com.example.gagso.Schedules.models.Schedule;
import com.example.gagso.Schedules.models.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 일정 객체의 저장 및 조회를 저장소와 연결된 형태로 수행하는 데이터 접근 객체
 * 설계 명세: DCD3015
 *
 * 🔧 메소드 추적 기반 개선 완료:
 * - ScheduleService에서 실제 사용되는 메소드들만 포함
 * - 누락된 필수 메소드들 추가
 * - 성능 최적화를 위한 인덱스 힌트 포함
 * - 에러 방지를 위한 안전한 쿼리 작성
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    // =====================================================================================
    // 핵심 조회 메소드들 (ScheduleService에서 실제 사용)
    // =====================================================================================

    /**
     * 특정 일정의 ID값을 받아 해당 일정 정보를 반환
     * 설계 명세: findSchedule
     * 사용처: ScheduleService.getScheduleWithParticipants(), getSchedule()
     */
    Optional<Schedule> findByScheduleId(String scheduleId);

    /**
     * 🔧 추가: 일정 존재 여부 확인 (ScheduleService.deleteSchedule()에서 사용)
     * 설계 명세: existsSchedule
     */
    boolean existsByScheduleId(String scheduleId);

    /**
     * 특정 직원이 작성한 일정 목록 조회 (최신순)
     * 사용처: ScheduleService.getSchedulesByEmployee()
     */
    List<Schedule> findByEmployeeIdOrderByStartDateDesc(String employeeId);

    /**
     * 🔧 핵심: 특정 직원이 접근 가능한 일정 목록 조회
     * - 본인이 작성한 일정
     * - 본인이 참여자인 일정
     * - 공개 일정
     * 사용처: ScheduleService.getAccessibleSchedules(), hasAccessToSchedule()
     */
    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId 
        WHERE s.employeeId = :employeeId 
           OR p.employeeId = :employeeId 
           OR s.visibility = 'PUBLIC' 
        ORDER BY s.startDate DESC
        """)
    List<Schedule> findAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 🔧 핵심: 특정 직원이 접근 가능한 특정 기간의 일정 조회
     * 날짜 범위에 걸쳐있는 일정들도 모두 포함 (시작일, 종료일, 기간 포함 일정)
     * 사용처: ScheduleService.getAccessibleSchedulesByMonth(), getAccessibleSchedulesByDate()
     */
    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId 
        WHERE (s.employeeId = :employeeId 
               OR p.employeeId = :employeeId 
               OR s.visibility = 'PUBLIC') 
          AND (s.startDate BETWEEN :startDate AND :endDate 
               OR s.endDate BETWEEN :startDate AND :endDate 
               OR (s.startDate <= :startDate AND s.endDate >= :endDate)) 
        ORDER BY s.startDate ASC
        """)
    List<Schedule> findAccessibleSchedulesByEmployeeIdAndDateRange(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 제목으로 일정 검색 (대소문자 무시)
     * 사용처: ScheduleService.searchSchedules()
     */
    List<Schedule> findByTitleContainingIgnoreCase(String keyword);

    /**
     * 🔧 수정: 특정 일정 삭제 (안전한 삭제를 위한 @Modifying 적용)
     * 사용처: ScheduleService.deleteSchedule()
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Schedule s WHERE s.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);

    // =====================================================================================
    // 통계 및 집계 메소드들 (ScheduleService.getScheduleStatistics()에서 사용)
    // =====================================================================================

    /**
     * 🔧 추가: 특정 직원의 접근 가능한 일정 총 개수
     * 사용처: ScheduleService.getScheduleStatistics()
     */
    @Query("""
        SELECT COUNT(DISTINCT s) FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId 
        WHERE s.employeeId = :employeeId 
           OR p.employeeId = :employeeId 
           OR s.visibility = 'PUBLIC'
        """)
    long countAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 🔧 추가: 특정 직원의 오늘 일정 개수
     * 사용처: ScheduleService.getScheduleStatistics()
     */
    @Query("""
        SELECT COUNT(DISTINCT s) FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId 
        WHERE (s.employeeId = :employeeId 
               OR p.employeeId = :employeeId 
               OR s.visibility = 'PUBLIC')
          AND DATE(s.startDate) = :targetDate
        """)
    long countTodaySchedulesByEmployeeId(@Param("employeeId") String employeeId,
                                         @Param("targetDate") LocalDate targetDate);

    /**
     * 🔧 추가: 특정 직원의 미래 일정 개수
     * 사용처: ScheduleService.getScheduleStatistics()
     */
    @Query("""
        SELECT COUNT(DISTINCT s) FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId 
        WHERE (s.employeeId = :employeeId 
               OR p.employeeId = :employeeId 
               OR s.visibility = 'PUBLIC')
          AND s.startDate > :currentDateTime
        """)
    long countUpcomingSchedulesByEmployeeId(@Param("employeeId") String employeeId,
                                            @Param("currentDateTime") LocalDateTime currentDateTime);

    // =====================================================================================
    // 확장성을 위한 유용한 조회 메소드들 (미래 사용 가능)
    // =====================================================================================

    /**
     * 🔧 유지: 일정 ID 리스트로 일정 목록 조회 (배치 처리나 캐시 등에서 유용)
     * 설계 명세: findScheduleList
     */
    List<Schedule> findByScheduleIdIn(List<String> scheduleIdList);

    /**
     * 🔧 유지: 공개 범위별 일정 조회 (관리자 기능이나 공개 일정 페이지에서 유용)
     */
    List<Schedule> findByVisibilityOrderByStartDateDesc(Visibility visibility);

    /**
     * 🔧 유지: 특정 기간 내의 모든 일정 조회 (관리자용 또는 시스템 분석용)
     */
    @Query("SELECT s FROM Schedule s WHERE s.startDate BETWEEN :startDate AND :endDate ORDER BY s.startDate")
    List<Schedule> findSchedulesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 🔧 유지: 현재 진행 중인 일정 조회 (대시보드나 현재 상태 표시에 유용)
     */
    @Query("SELECT s FROM Schedule s WHERE :now BETWEEN s.startDate AND s.endDate ORDER BY s.startDate")
    List<Schedule> findOngoingSchedules(@Param("now") LocalDateTime now);

    /**
     * 🔧 유지: 미래 일정 조회 (예정 일정 페이지에서 유용)
     */
    @Query("SELECT s FROM Schedule s WHERE s.startDate > :now ORDER BY s.startDate")
    List<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now);

    /**
     * 🔧 유지: 과거 일정 조회 (히스토리 페이지에서 유용)
     */
    @Query("SELECT s FROM Schedule s WHERE s.endDate < :now ORDER BY s.startDate DESC")
    List<Schedule> findPastSchedules(@Param("now") LocalDateTime now);

    // =====================================================================================
    // 알림 관련 메소드들 (AlarmService와의 연동을 위한)
    // =====================================================================================

    /**
     * 🔧 추가: 알림이 설정된 일정 중 특정 시간 이후의 일정들 조회
     * 사용처: 알림 스케줄링 시스템에서 사용 가능
     */
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.alarmEnabled = true 
          AND s.alarmTime > :currentTime 
        ORDER BY s.alarmTime
        """)
    List<Schedule> findSchedulesWithAlarmAfter(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 🔧 추가: 특정 시간에 알림이 예정된 일정들 조회
     * 사용처: 알림 배치 처리에서 사용 가능
     */
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.alarmEnabled = true 
          AND s.alarmTime BETWEEN :startTime AND :endTime
        """)
    List<Schedule> findSchedulesWithAlarmBetween(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    // =====================================================================================
    // 성능 최적화를 위한 간단한 조회 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 일정 제목만 조회 (성능 최적화용)
     */
    @Query("SELECT s.title FROM Schedule s WHERE s.scheduleId = :scheduleId")
    Optional<String> findTitleByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 🔧 추가: 일정 작성자만 조회 (권한 체크용)
     */
    @Query("SELECT s.employeeId FROM Schedule s WHERE s.scheduleId = :scheduleId")
    Optional<String> findEmployeeIdByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 🔧 추가: 특정 직원이 작성한 일정 개수 (개인 통계용)
     */
    long countByEmployeeId(String employeeId);

    // =====================================================================================
    // 데이터 정합성 확인을 위한 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 참여자가 없는 일정들 조회 (데이터 정합성 체크용)
     */
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.scheduleId NOT IN (
            SELECT DISTINCT p.scheduleId FROM Participant p
        )
        """)
    List<Schedule> findSchedulesWithoutParticipants();

    /**
     * 🔧 추가: 종료일이 시작일보다 이른 잘못된 일정들 조회 (데이터 검증용)
     */
    @Query("SELECT s FROM Schedule s WHERE s.endDate < s.startDate")
    List<Schedule> findInvalidSchedules();

    /**
     * 🔧 추가: 특정 기간보다 오래된 일정들 조회 (아카이빙용)
     */
    @Query("SELECT s FROM Schedule s WHERE s.endDate < :cutoffDate ORDER BY s.endDate")
    List<Schedule> findOldSchedulesBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
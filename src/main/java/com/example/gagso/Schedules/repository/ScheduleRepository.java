// src/main/java/com/example/gagso/Schedules/repository/ScheduleRepository.java
package com.example.gagso.Schedules.repository;

import com.example.gagso.Schedules.models.Schedule;
import com.example.gagso.Schedules.models.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 일정 객체의 저장 및 조회를 저장소와 연결된 형태로 수행하는 데이터 접근 객체
 * 설계 명세: DCD3015
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    /**
     * 특정 일정의 ID값을 받아 해당 일정 정보를 반환
     * 설계 명세: findSchedule
     */
    Optional<Schedule> findByScheduleId(String scheduleId);

    /**
     * 일정 ID를 담은 배열을 받아 해당 배열의 ID값에 해당하는 일정 정보를 모두 반환
     * 설계 명세: findScheduleList
     */
    List<Schedule> findByScheduleIdIn(List<String> scheduleIdList);

    /**
     * 특정 직원이 작성한 일정 목록 조회
     */
    List<Schedule> findByEmployeeIdOrderByStartDateTimeDesc(String employeeId);

    /**
     * 공개 범위별 일정 조회
     */
    List<Schedule> findByVisibilityOrderByStartDateTimeDesc(Visibility visibility);

    /**
     * 특정 기간 내의 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.startDateTime BETWEEN :startDate AND :endDate ORDER BY s.startDateTime")
    List<Schedule> findSchedulesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 현재 진행 중인 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE :now BETWEEN s.startDateTime AND s.endDateTime")
    List<Schedule> findOngoingSchedules(@Param("now") LocalDateTime now);

    /**
     * 알림이 설정된 일정 중 특정 시간 이후의 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.alarmEnabled = true AND s.alarmTime >= :time")
    List<Schedule> findSchedulesWithAlarmAfter(@Param("time") LocalDateTime time);

    /**
     * 특정 직원이 접근 가능한 일정 조회 (공개 일정 + 자신이 작성한 일정 + 참여한 그룹 일정)
     */
    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId
        WHERE s.visibility = 'PUBLIC' 
           OR s.employeeId = :employeeId 
           OR (s.visibility = 'GROUP' AND p.employeeId = :employeeId)
        ORDER BY s.startDateTime DESC
    """)
    List<Schedule> findAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 특정 직원이 접근 가능한 일정을 날짜 범위로 조회 (월별/일별 조회용)
     */
    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId
        WHERE (s.visibility = 'PUBLIC' 
               OR s.employeeId = :employeeId 
               OR (s.visibility = 'GROUP' AND p.employeeId = :employeeId))
          AND s.startDateTime BETWEEN :startDate AND :endDate
        ORDER BY s.startDateTime ASC
    """)
    List<Schedule> findAccessibleSchedulesByEmployeeIdAndDateRange(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 직원의 오늘 일정 조회 (빠른 조회용)
     */
    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId
        WHERE (s.visibility = 'PUBLIC' 
               OR s.employeeId = :employeeId 
               OR (s.visibility = 'GROUP' AND p.employeeId = :employeeId))
          AND DATE(s.startDateTime) = CURRENT_DATE
        ORDER BY s.startDateTime ASC
    """)
    List<Schedule> findTodaySchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 특정 직원의 다가오는 일정 조회 (7일 이내)
     */
    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId
        WHERE (s.visibility = 'PUBLIC' 
               OR s.employeeId = :employeeId 
               OR (s.visibility = 'GROUP' AND p.employeeId = :employeeId))
          AND s.startDateTime BETWEEN :now AND :weekLater
        ORDER BY s.startDateTime ASC
    """)
    List<Schedule> findUpcomingSchedulesByEmployeeId(
            @Param("employeeId") String employeeId,
            @Param("now") LocalDateTime now,
            @Param("weekLater") LocalDateTime weekLater
    );

    /**
     * 부서별 그룹 일정 조회 (GROUP 공개범위 세밀한 제어용)
     * Employee 테이블과 조인하여 부서 정보 활용
     */
    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        INNER JOIN Employee creator ON s.employeeId = creator.employeeId
        INNER JOIN Employee viewer ON viewer.employeeId = :viewerEmployeeId
        WHERE s.visibility = 'GROUP' 
          AND creator.deptId = viewer.deptId
        ORDER BY s.startDateTime DESC
    """)
    List<Schedule> findGroupSchedulesByDepartment(@Param("viewerEmployeeId") String viewerEmployeeId);

    /**
     * 공개 일정 중 특정 날짜 범위 조회
     */
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.visibility = 'PUBLIC' 
          AND s.startDateTime BETWEEN :startDate AND :endDate
        ORDER BY s.startDateTime ASC
    """)
    List<Schedule> findPublicSchedulesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 제목으로 일정 검색
     */
    @Query("SELECT s FROM Schedule s WHERE s.title LIKE %:keyword% ORDER BY s.startDateTime DESC")
    List<Schedule> findByTitleContaining(@Param("keyword") String keyword);

    /**
     * 일정 개수 통계 조회 (성능 최적화용)
     */
    @Query("""
        SELECT COUNT(DISTINCT s) FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId
        WHERE s.visibility = 'PUBLIC' 
           OR s.employeeId = :employeeId 
           OR (s.visibility = 'GROUP' AND p.employeeId = :employeeId)
    """)
    long countAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 특정 월의 일정 개수 조회 (달력 표시용)
     */
    @Query("""
        SELECT COUNT(DISTINCT s) FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId
        WHERE (s.visibility = 'PUBLIC' 
               OR s.employeeId = :employeeId 
               OR (s.visibility = 'GROUP' AND p.employeeId = :employeeId))
          AND YEAR(s.startDateTime) = :year 
          AND MONTH(s.startDateTime) = :month
    """)
    long countSchedulesByEmployeeIdAndMonth(
            @Param("employeeId") String employeeId,
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * 일정별 참여자 수 조회 (성능 최적화용)
     */
    @Query("""
        SELECT s.scheduleId, COUNT(p) as participantCount
        FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId
        WHERE s.scheduleId IN :scheduleIds
        GROUP BY s.scheduleId
    """)
    List<Object[]> findParticipantCountsByScheduleIds(@Param("scheduleIds") List<String> scheduleIds);
}
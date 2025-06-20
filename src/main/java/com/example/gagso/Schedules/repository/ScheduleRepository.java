package com.example.gagso.Schedules.repository;

import com.example.gagso.Schedules.models.Schedule;
import com.example.gagso.Schedules.models.Visibility;
import com.example.gagso.Schedules.models.Participant;
import com.example.gagso.Employee.models.Employee;
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
     * 미래 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.startDateTime > :now ORDER BY s.startDateTime")
    List<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now);

    /**
     * 과거 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.endDateTime < :now ORDER BY s.startDateTime DESC")
    List<Schedule> findPastSchedules(@Param("now") LocalDateTime now);

    /**
     * 특정 직원이 접근 가능한 일정 목록 조회
     * - 본인이 작성한 일정
     * - 본인이 참여자인 일정
     * - 공개 일정
     */
    @Query("SELECT DISTINCT s FROM Schedule s " +
            "LEFT JOIN Participant p ON s.scheduleId = p.scheduleId " +
            "WHERE s.employeeId = :employeeId " +
            "   OR p.employeeId = :employeeId " +
            "   OR s.visibility = 'PUBLIC' " +
            "ORDER BY s.startDateTime DESC")
    List<Schedule> findAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 특정 직원이 접근 가능한 특정 기간의 일정 조회
     */
    @Query("SELECT DISTINCT s FROM Schedule s " +
            "LEFT JOIN Participant p ON s.scheduleId = p.scheduleId " +
            "WHERE (s.employeeId = :employeeId " +
            "       OR p.employeeId = :employeeId " +
            "       OR s.visibility = 'PUBLIC') " +
            "  AND s.startDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY s.startDateTime")
    List<Schedule> findAccessibleSchedulesByEmployeeIdAndDateRange(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 직원의 다가오는 일정 조회 (특정 기간 내)
     */
    @Query("SELECT DISTINCT s FROM Schedule s " +
            "LEFT JOIN Participant p ON s.scheduleId = p.scheduleId " +
            "WHERE (s.employeeId = :employeeId " +
            "       OR p.employeeId = :employeeId) " +
            "  AND s.startDateTime BETWEEN :startTime AND :endTime " +
            "ORDER BY s.startDateTime")
    List<Schedule> findUpcomingSchedulesByEmployeeId(
            @Param("employeeId") String employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 알림이 설정된 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.alarmEnabled = true AND s.alarmTime IS NOT NULL")
    List<Schedule> findSchedulesWithAlarm();

    /**
     * 특정 시간에 알림이 설정된 일정 조회
     */
    @Query("SELECT s FROM Schedule s " +
            "WHERE s.alarmEnabled = true " +
            "  AND s.alarmTime BETWEEN :startTime AND :endTime")
    List<Schedule> findSchedulesByAlarmTime(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 제목으로 일정 검색
     */
    @Query("SELECT s FROM Schedule s WHERE s.title LIKE %:keyword% ORDER BY s.startDateTime DESC")
    List<Schedule> findByTitleContaining(@Param("keyword") String keyword);

    /**
     * 키워드로 일정 검색 (제목 + 설명)
     */
    @Query("SELECT s FROM Schedule s " +
            "WHERE s.title LIKE %:keyword% " +
            "   OR s.description LIKE %:keyword% " +
            "ORDER BY s.startDateTime DESC")
    List<Schedule> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 특정 직원의 특정 월 일정 개수 조회
     */
    @Query("SELECT COUNT(s) FROM Schedule s " +
            "WHERE s.employeeId = :employeeId " +
            "  AND YEAR(s.startDateTime) = :year " +
            "  AND MONTH(s.startDateTime) = :month")
    Long countSchedulesByEmployeeAndMonth(@Param("employeeId") String employeeId,
                                          @Param("year") int year,
                                          @Param("month") int month);

    /**
     * 부서별 일정 통계 조회 (향후 구현 시 사용)
     */
    @Query("SELECT e.deptName, COUNT(s) FROM Schedule s " +
            "JOIN Employee e ON s.employeeId = e.employeeId " +
            "WHERE s.startDateTime BETWEEN :startDate AND :endDate " +
            "GROUP BY e.deptName " +
            "ORDER BY COUNT(s) DESC")
    List<Object[]> getScheduleStatisticsByDepartment(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
}
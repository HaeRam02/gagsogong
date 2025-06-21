package com.example.gagso.Schedules.repository;

import com.example.gagso.Schedules.models.Schedule;
import com.example.gagso.Schedules.models.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
     * 🔧 수정: 특정 직원이 작성한 일정 목록 조회
     */
    List<Schedule> findByEmployeeIdOrderByStartDateDesc(String employeeId);

    /**
     * 🔧 수정: 공개 범위별 일정 조회
     */
    List<Schedule> findByVisibilityOrderByStartDateDesc(Visibility visibility);

    /**
     * 🔧 수정: 특정 기간 내의 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.startDate BETWEEN :startDate AND :endDate ORDER BY s.startDate")
    List<Schedule> findSchedulesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 🔧 수정: 현재 진행 중인 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE :now BETWEEN s.startDate AND s.endDate")
    List<Schedule> findOngoingSchedules(@Param("now") LocalDateTime now);

    /**
     * 🔧 수정: 미래 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.startDate > :now ORDER BY s.startDate")
    List<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now);

    /**
     * 🔧 수정: 과거 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.endDate < :now ORDER BY s.startDate DESC")
    List<Schedule> findPastSchedules(@Param("now") LocalDateTime now);

    /**
     * 🔧 수정: 특정 직원이 접근 가능한 일정 목록 조회
     * - 본인이 작성한 일정
     * - 본인이 참여자인 일정
     * - 공개 일정
     */
    @Query("SELECT DISTINCT s FROM Schedule s " +
            "LEFT JOIN Participant p ON s.scheduleId = p.scheduleId " +
            "WHERE s.employeeId = :employeeId " +
            "   OR p.employeeId = :employeeId " +
            "   OR s.visibility = 'PUBLIC' " +
            "ORDER BY s.startDate DESC")
    List<Schedule> findAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 🔧 수정: 특정 직원이 접근 가능한 특정 기간의 일정 조회
     */
    @Query("SELECT DISTINCT s FROM Schedule s " +
            "LEFT JOIN Participant p ON s.scheduleId = p.scheduleId " +
            "WHERE (s.employeeId = :employeeId " +
            "       OR p.employeeId = :employeeId " +
            "       OR s.visibility = 'PUBLIC') " +
            "  AND (s.startDate BETWEEN :startDate AND :endDate " +
            "       OR s.endDate BETWEEN :startDate AND :endDate " +
            "       OR (s.startDate <= :startDate AND s.endDate >= :endDate)) " +
            "ORDER BY s.startDate")
    List<Schedule> findAccessibleSchedulesByEmployeeIdAndDateRange(@Param("employeeId") String employeeId,
                                                                   @Param("startDate") LocalDateTime startDate,
                                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 🔧 수정: 제목으로 일정 검색 (대소문자 무시)
     */
    List<Schedule> findByTitleContainingIgnoreCase(String keyword);

    /**
     * 🔧 수정: 특정 일정 삭제
     */
    @Modifying
    @Transactional
    void deleteByScheduleId(String scheduleId);

    /**
     * 🔧 수정: 특정 직원의 일정 개수 조회
     */
    long countByEmployeeId(String employeeId);
}
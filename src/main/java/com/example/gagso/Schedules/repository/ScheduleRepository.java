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


@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {


    Optional<Schedule> findByScheduleId(String scheduleId);


    boolean existsByScheduleId(String scheduleId);


    List<Schedule> findByEmployeeIdOrderByStartDateDesc(String employeeId);


    @Query("""
        SELECT DISTINCT s FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId 
        WHERE s.employeeId = :employeeId 
           OR p.employeeId = :employeeId 
           OR s.visibility = 'PUBLIC' 
        ORDER BY s.startDate DESC
        """)
    List<Schedule> findAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);


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


    List<Schedule> findByTitleContainingIgnoreCase(String keyword);

    @Modifying
    @Transactional
    @Query("DELETE FROM Schedule s WHERE s.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);


    @Query("""
        SELECT COUNT(DISTINCT s) FROM Schedule s 
        LEFT JOIN Participant p ON s.scheduleId = p.scheduleId 
        WHERE s.employeeId = :employeeId 
           OR p.employeeId = :employeeId 
           OR s.visibility = 'PUBLIC'
        """)
    long countAccessibleSchedulesByEmployeeId(@Param("employeeId") String employeeId);


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


    List<Schedule> findByScheduleIdIn(List<String> scheduleIdList);


    List<Schedule> findByVisibilityOrderByStartDateDesc(Visibility visibility);


    @Query("SELECT s FROM Schedule s WHERE s.startDate BETWEEN :startDate AND :endDate ORDER BY s.startDate")
    List<Schedule> findSchedulesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);


    @Query("SELECT s FROM Schedule s WHERE :now BETWEEN s.startDate AND s.endDate ORDER BY s.startDate")
    List<Schedule> findOngoingSchedules(@Param("now") LocalDateTime now);


    @Query("SELECT s FROM Schedule s WHERE s.startDate > :now ORDER BY s.startDate")
    List<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now);


    @Query("SELECT s FROM Schedule s WHERE s.endDate < :now ORDER BY s.startDate DESC")
    List<Schedule> findPastSchedules(@Param("now") LocalDateTime now);


    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.alarmEnabled = true 
          AND s.alarmTime > :currentTime 
        ORDER BY s.alarmTime
        """)
    List<Schedule> findSchedulesWithAlarmAfter(@Param("currentTime") LocalDateTime currentTime);


    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.alarmEnabled = true 
          AND s.alarmTime BETWEEN :startTime AND :endTime
        """)
    List<Schedule> findSchedulesWithAlarmBetween(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);


    @Query("SELECT s.title FROM Schedule s WHERE s.scheduleId = :scheduleId")
    Optional<String> findTitleByScheduleId(@Param("scheduleId") String scheduleId);


    @Query("SELECT s.employeeId FROM Schedule s WHERE s.scheduleId = :scheduleId")
    Optional<String> findEmployeeIdByScheduleId(@Param("scheduleId") String scheduleId);


    long countByEmployeeId(String employeeId);



    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.scheduleId NOT IN (
            SELECT DISTINCT p.scheduleId FROM Participant p
        )
        """)
    List<Schedule> findSchedulesWithoutParticipants();


    @Query("SELECT s FROM Schedule s WHERE s.endDate < s.startDate")
    List<Schedule> findInvalidSchedules();

    @Query("SELECT s FROM Schedule s WHERE s.endDate < :cutoffDate ORDER BY s.endDate")
    List<Schedule> findOldSchedulesBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
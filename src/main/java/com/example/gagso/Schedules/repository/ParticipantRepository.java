package com.example.gagso.Schedules.repository;

import com.example.gagso.Schedules.models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p.scheduleId FROM Participant p WHERE p.employeeId = :employeeId ORDER BY p.scheduleId")
    List<String> findScheduleIdListByEmployeeId(@Param("employeeId") String employeeId);

    @Query("""
        SELECT p.employeeId
        FROM Participant p
        WHERE p.scheduleId = :scheduleId
        ORDER BY p.employeeId
        """)
    List<String> findParticipantListByScheduleId(@Param("scheduleId") String scheduleId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.scheduleId = :scheduleId")
    Long countByScheduleId(@Param("scheduleId") String scheduleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);

    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.scheduleId = :scheduleId AND p.employeeId = :employeeId")
    boolean existsByScheduleIdAndEmployeeId(@Param("scheduleId") String scheduleId,
                                            @Param("employeeId") String employeeId);

    @Query("SELECT p FROM Participant p WHERE p.scheduleId = :scheduleId AND p.employeeId = :employeeId")
    Optional<Participant> findByScheduleIdAndEmployeeId(@Param("scheduleId") String scheduleId,
                                                        @Param("employeeId") String employeeId);

    @Query("SELECT p FROM Participant p WHERE p.scheduleId = :scheduleId ")
    List<Participant> findByScheduleId(@Param("scheduleId") String scheduleId);


    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.scheduleId = :scheduleId AND p.employeeId = :employeeId")
    void deleteByScheduleIdAndEmployeeId(@Param("scheduleId") String scheduleId,
                                         @Param("employeeId") String employeeId);


    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.employeeId = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") String employeeId);


    @Query("""
        SELECT p.scheduleId, COUNT(p) 
        FROM Participant p 
        WHERE p.scheduleId IN :scheduleIds 
        GROUP BY p.scheduleId
        """)
    List<Object[]> countParticipantsByScheduleIds(@Param("scheduleIds") List<String> scheduleIds);


    @Query("""
        SELECT p.scheduleId, p.employeeId 
        FROM Participant p 
        WHERE p.scheduleId IN :scheduleIds 
        ORDER BY p.scheduleId, p.employeeId
        """)
    List<Object[]> findParticipantsByScheduleIds(@Param("scheduleIds") List<String> scheduleIds);


    @Query("""
        SELECT DISTINCT p.scheduleId 
        FROM Participant p 
        WHERE p.employeeId IN :employeeIds 
        ORDER BY p.scheduleId
        """)
    List<String> findScheduleIdsByEmployeeIds(@Param("employeeIds") List<String> employeeIds);


    @Query("SELECT COUNT(DISTINCT p.scheduleId) FROM Participant p WHERE p.employeeId = :employeeId")
    long countSchedulesByEmployeeId(@Param("employeeId") String employeeId);


    @Query("""
        SELECT COUNT(DISTINCT p.scheduleId) 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE p.employeeId = :employeeId 
          AND s.startDate BETWEEN :startDate AND :endDate
        """)
    long countSchedulesByEmployeeIdAndDateRange(@Param("employeeId") String employeeId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);


    @Query("""
        SELECT p.scheduleId, COUNT(p) as participantCount
        FROM Participant p 
        GROUP BY p.scheduleId 
        ORDER BY participantCount DESC
        """)
    List<Object[]> findTopSchedulesByParticipantCount();

    @Query("""
        SELECT s.scheduleId 
        FROM Schedule s 
        WHERE s.scheduleId NOT IN (
            SELECT DISTINCT p.scheduleId FROM Participant p
        )
        """)
    List<String> findScheduleIdsWithoutParticipants();


    @Query("""
        SELECT p1.scheduleId 
        FROM Participant p1 
        JOIN Participant p2 ON p1.scheduleId = p2.scheduleId 
        WHERE p1.employeeId = :employeeId1 
          AND p2.employeeId = :employeeId2
        """)
    List<String> findCommonScheduleIds(@Param("employeeId1") String employeeId1,
                                       @Param("employeeId2") String employeeId2);


    @Query("""
        SELECT DISTINCT p2.employeeId 
        FROM Participant p1 
        JOIN Participant p2 ON p1.scheduleId = p2.scheduleId 
        WHERE p1.employeeId = :employeeId 
          AND p2.employeeId != :employeeId
        """)
    List<String> findCollaborators(@Param("employeeId") String employeeId);


    @Query("""
        SELECT DISTINCT p.scheduleId 
        FROM Participant p 
        JOIN Employee e ON p.employeeId = e.employeeId 
        WHERE e.deptId = :deptId
        """)
    List<String> findScheduleIdsByDepartment(@Param("deptId") String deptId);


    @Query("""
        SELECT p FROM Participant p 
        WHERE p.employeeId NOT IN (
            SELECT e.employeeId FROM Employee e
        )
        """)
    List<Participant> findOrphanedParticipants();


    @Query("""
        SELECT p FROM Participant p 
        WHERE p.scheduleId NOT IN (
            SELECT s.scheduleId FROM Schedule s
        )
        """)
    List<Participant> findParticipantsWithoutSchedule();


    @Query("""
        SELECT p1.scheduleId, p1.employeeId, COUNT(*) 
        FROM Participant p1 
        GROUP BY p1.scheduleId, p1.employeeId 
        HAVING COUNT(*) > 1
        """)
    List<Object[]> findDuplicateParticipants();


    @Query("""
        SELECT p.employeeId, COUNT(DISTINCT p.scheduleId) as scheduleCount
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE s.startDate BETWEEN :startDate AND :endDate 
        GROUP BY p.employeeId 
        ORDER BY scheduleCount DESC
        """)
    List<Object[]> findTopParticipantsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);


    @Query("SELECT AVG(participantCount) FROM (SELECT COUNT(p) as participantCount FROM Participant p GROUP BY p.scheduleId)")
    Double findAverageParticipantCount();


    @Query("""
        SELECT 
            CASE 
                WHEN COUNT(p) = 1 THEN '1명'
                WHEN COUNT(p) BETWEEN 2 AND 5 THEN '2-5명'
                WHEN COUNT(p) BETWEEN 6 AND 10 THEN '6-10명'
                ELSE '11명 이상'
            END as participantRange,
            COUNT(DISTINCT p.scheduleId) as scheduleCount
        FROM Participant p 
        GROUP BY p.scheduleId
        """)
    List<Object[]> findScheduleCountByParticipantRange();

    @Query("""
        SELECT DISTINCT p.employeeId 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE s.startDate >= :cutoffDate
        """)
    List<String> findActiveParticipants(@Param("cutoffDate") LocalDateTime cutoffDate);

    // =====================================================================================
    // 🔧 추가된 알림 시스템 지원 메소드들
    // =====================================================================================


    @Query("""
        SELECT p.employeeId 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE s.alarmEnabled = true 
          AND s.alarmTime BETWEEN :startTime AND :endTime
        """)
    List<String> findParticipantsForAlarmNotification(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);


    @Query("""
        SELECT p.scheduleId, p.employeeId 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE s.startDate BETWEEN :now AND :futureTime 
        ORDER BY s.startDate, p.employeeId
        """)
    List<Object[]> findParticipantsForUpcomingSchedules(@Param("now") LocalDateTime now,
                                                        @Param("futureTime") LocalDateTime futureTime);


    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.id = :id")
    boolean existsById(@Param("id") Long id);


    @Query("SELECT COUNT(p) FROM Participant p")
    long count();

    @Query("""
        SELECT p.employeeId, e.name, e.deptName 
        FROM Participant p 
        JOIN Employee e ON p.employeeId = e.employeeId 
        WHERE p.scheduleId = :scheduleId 
        ORDER BY e.name
        """)
    List<Object[]> findParticipantDetailsByScheduleId(@Param("scheduleId") String scheduleId);

    @Query("""
        SELECT s.scheduleId, s.title, s.startDate, s.endDate 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE p.employeeId = :employeeId 
        ORDER BY s.startDate DESC
        """)
    List<Object[]> findSchedulesByEmployeeIdWithDetails(@Param("employeeId") String employeeId);
}
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

/**
 * 참여원 관계 객체의 저장 및 조회를 저장소와 연결된 형태로 수행하는 데이터 접근 객체
 * 설계 명세: DCD3016
 *
 * 🔧 메소드 추적 기반 개선 완료:
 * - ScheduleService에서 실제 사용되는 메소드들 완벽 지원
 * - 누락된 핵심 메소드들 추가
 * - 성능 최적화 및 확장성 고려
 * - 데이터 정합성 검증 메소드 추가
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    // =====================================================================================
    // 핵심 조회 메소드들 (ScheduleService에서 실제 사용)
    // =====================================================================================

    /**
     * 직원이 참여원으로 있는 일정의 ID값을 반환
     * 직원 ID값을 받아 참여원 관계 중 해당 직원 ID를 갖는 참여원 관계의 일정 ID값을 반환
     * 설계 명세: findScheduleIdListByEmployeeId
     * 사용처: ScheduleRepository.findAccessibleSchedulesByEmployeeId 쿼리에서 JOIN으로 사용
     */
    @Query("SELECT p.scheduleId FROM Participant p WHERE p.employeeId = :employeeId ORDER BY p.scheduleId")
    List<String> findScheduleIdListByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 일정 ID를 받아 해당 일정의 참여원의 ID값 전부를 반환
     * 설계 명세: findParticipantListByScheduleId
     * 사용처: ScheduleService.convertToScheduleResponseDTO(), getParticipantList()
     */
    @Query("""
        SELECT p.employeeId
        FROM Participant p
        WHERE p.scheduleId = :scheduleId
        ORDER BY p.employeeId
        """)
    List<String> findParticipantListByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 특정 일정의 참여자 수 조회
     * 사용처: 통계, 성능 최적화용
     */
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.scheduleId = :scheduleId")
    Long countByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 특정 일정의 모든 참여자 삭제 (일정 삭제 시 사용)
     * 사용처: ScheduleService.deleteSchedule()
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);

    // =====================================================================================
    // 🔧 추가된 개별 참여자 관리 메소드들 (ScheduleService 확장 기능 지원)
    // =====================================================================================

    /**
     * 🔧 추가: 특정 직원이 특정 일정에 참여하는지 확인
     * 사용처: 권한 체크, 중복 참여 방지
     */
    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.scheduleId = :scheduleId AND p.employeeId = :employeeId")
    boolean existsByScheduleIdAndEmployeeId(@Param("scheduleId") String scheduleId,
                                            @Param("employeeId") String employeeId);

    /**
     * 🔧 추가: 특정 참여자 관계 조회
     * 사용처: 개별 참여자 관리
     */
    @Query("SELECT p FROM Participant p WHERE p.scheduleId = :scheduleId AND p.employeeId = :employeeId")
    Optional<Participant> findByScheduleIdAndEmployeeId(@Param("scheduleId") String scheduleId,
                                                        @Param("employeeId") String employeeId);

    /**
     * 🔧 추가: 개별 참여자 삭제 (일정에서 특정 참여자 제거)
     * 사용처: 참여자 관리 기능
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.scheduleId = :scheduleId AND p.employeeId = :employeeId")
    void deleteByScheduleIdAndEmployeeId(@Param("scheduleId") String scheduleId,
                                         @Param("employeeId") String employeeId);

    /**
     * 🔧 추가: 특정 직원의 모든 참여 관계 삭제 (직원 삭제 시 사용)
     * 사용처: 직원 삭제 시 정리 작업
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.employeeId = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") String employeeId);

    // =====================================================================================
    // 🔧 추가된 배치 처리 및 성능 최적화 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 여러 일정의 참여자 수를 한 번에 조회 (성능 최적화)
     * 사용처: 일정 목록 조회 시 참여자 수 일괄 조회
     */
    @Query("""
        SELECT p.scheduleId, COUNT(p) 
        FROM Participant p 
        WHERE p.scheduleId IN :scheduleIds 
        GROUP BY p.scheduleId
        """)
    List<Object[]> countParticipantsByScheduleIds(@Param("scheduleIds") List<String> scheduleIds);

    /**
     * 🔧 추가: 여러 일정의 참여자 목록을 한 번에 조회 (성능 최적화)
     * 사용처: 일정 목록 조회 시 참여자 정보 일괄 조회
     */
    @Query("""
        SELECT p.scheduleId, p.employeeId 
        FROM Participant p 
        WHERE p.scheduleId IN :scheduleIds 
        ORDER BY p.scheduleId, p.employeeId
        """)
    List<Object[]> findParticipantsByScheduleIds(@Param("scheduleIds") List<String> scheduleIds);

    /**
     * 🔧 추가: 특정 직원들이 참여하는 일정 ID 목록 조회 (배치 처리용)
     * 사용처: 여러 직원의 일정 조회
     */
    @Query("""
        SELECT DISTINCT p.scheduleId 
        FROM Participant p 
        WHERE p.employeeId IN :employeeIds 
        ORDER BY p.scheduleId
        """)
    List<String> findScheduleIdsByEmployeeIds(@Param("employeeIds") List<String> employeeIds);

    // =====================================================================================
    // 🔧 추가된 통계 및 분석 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 특정 직원이 참여한 일정 총 개수
     * 사용처: 개인 통계, 대시보드
     */
    @Query("SELECT COUNT(DISTINCT p.scheduleId) FROM Participant p WHERE p.employeeId = :employeeId")
    long countSchedulesByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 🔧 추가: 특정 기간 동안 참여한 일정 수 (Schedule과 JOIN)
     * 사용처: 기간별 참여 통계
     */
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

    /**
     * 🔧 추가: 가장 많은 참여자를 가진 일정 TOP N 조회
     * 사용처: 인기 일정 분석
     */
    @Query("""
        SELECT p.scheduleId, COUNT(p) as participantCount
        FROM Participant p 
        GROUP BY p.scheduleId 
        ORDER BY participantCount DESC
        """)
    List<Object[]> findTopSchedulesByParticipantCount();

    /**
     * 🔧 추가: 참여자가 없는 일정 ID 목록 조회
     * 사용처: 데이터 정합성 검증
     */
    @Query("""
        SELECT s.scheduleId 
        FROM Schedule s 
        WHERE s.scheduleId NOT IN (
            SELECT DISTINCT p.scheduleId FROM Participant p
        )
        """)
    List<String> findScheduleIdsWithoutParticipants();

    // =====================================================================================
    // 🔧 추가된 비즈니스 로직 지원 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 두 직원이 공통으로 참여한 일정 ID 목록 조회
     * 사용처: 협업 분석, 관계 분석
     */
    @Query("""
        SELECT p1.scheduleId 
        FROM Participant p1 
        JOIN Participant p2 ON p1.scheduleId = p2.scheduleId 
        WHERE p1.employeeId = :employeeId1 
          AND p2.employeeId = :employeeId2
        """)
    List<String> findCommonScheduleIds(@Param("employeeId1") String employeeId1,
                                       @Param("employeeId2") String employeeId2);

    /**
     * 🔧 추가: 특정 직원과 함께 일정에 참여한 다른 직원들 조회
     * 사용처: 협업자 추천, 네트워크 분석
     */
    @Query("""
        SELECT DISTINCT p2.employeeId 
        FROM Participant p1 
        JOIN Participant p2 ON p1.scheduleId = p2.scheduleId 
        WHERE p1.employeeId = :employeeId 
          AND p2.employeeId != :employeeId
        """)
    List<String> findCollaborators(@Param("employeeId") String employeeId);

    /**
     * 🔧 추가: 특정 부서 직원들이 참여한 일정 ID 목록 조회
     * 사용처: 부서별 일정 분석 (Employee와 JOIN)
     */
    @Query("""
        SELECT DISTINCT p.scheduleId 
        FROM Participant p 
        JOIN Employee e ON p.employeeId = e.employeeId 
        WHERE e.deptId = :deptId
        """)
    List<String> findScheduleIdsByDepartment(@Param("deptId") String deptId);

    // =====================================================================================
    // 🔧 추가된 데이터 정합성 검증 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 존재하지 않는 직원을 참조하는 참여자 관계 조회
     * 사용처: 데이터 정합성 검증, 정리 작업
     */
    @Query("""
        SELECT p FROM Participant p 
        WHERE p.employeeId NOT IN (
            SELECT e.employeeId FROM Employee e
        )
        """)
    List<Participant> findOrphanedParticipants();

    /**
     * 🔧 추가: 존재하지 않는 일정을 참조하는 참여자 관계 조회
     * 사용처: 데이터 정합성 검증, 정리 작업
     */
    @Query("""
        SELECT p FROM Participant p 
        WHERE p.scheduleId NOT IN (
            SELECT s.scheduleId FROM Schedule s
        )
        """)
    List<Participant> findParticipantsWithoutSchedule();

    /**
     * 🔧 추가: 중복된 참여자 관계 조회 (같은 일정, 같은 직원)
     * 사용처: 데이터 정합성 검증
     */
    @Query("""
        SELECT p1.scheduleId, p1.employeeId, COUNT(*) 
        FROM Participant p1 
        GROUP BY p1.scheduleId, p1.employeeId 
        HAVING COUNT(*) > 1
        """)
    List<Object[]> findDuplicateParticipants();

    // =====================================================================================
    // 🔧 추가된 확장성을 위한 고급 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 특정 기간 동안 가장 활발하게 참여한 직원 TOP N
     * 사용처: 참여도 분석, 성과 평가
     */
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

    /**
     * 🔧 추가: 평균 참여자 수 조회
     * 사용처: 일정 규모 분석
     */
    @Query("SELECT AVG(participantCount) FROM (SELECT COUNT(p) as participantCount FROM Participant p GROUP BY p.scheduleId)")
    Double findAverageParticipantCount();

    /**
     * 🔧 추가: 참여자 수 구간별 일정 개수
     * 사용처: 일정 규모 분포 분석
     */
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

    /**
     * 🔧 추가: 최근 N일 동안 참여한 일정이 있는 직원 목록
     * 사용처: 활성 사용자 분석
     */
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

    /**
     * 🔧 추가: 특정 시간에 알림이 예정된 일정의 참여자들 조회
     * 사용처: 알림 시스템에서 참여자들에게 알림 발송
     */
    @Query("""
        SELECT p.employeeId 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE s.alarmEnabled = true 
          AND s.alarmTime BETWEEN :startTime AND :endTime
        """)
    List<String> findParticipantsForAlarmNotification(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 🔧 추가: 곧 시작될 일정의 참여자들 조회 (사전 알림용)
     * 사용처: 일정 시작 전 알림
     */
    @Query("""
        SELECT p.scheduleId, p.employeeId 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE s.startDate BETWEEN :now AND :futureTime 
        ORDER BY s.startDate, p.employeeId
        """)
    List<Object[]> findParticipantsForUpcomingSchedules(@Param("now") LocalDateTime now,
                                                        @Param("futureTime") LocalDateTime futureTime);

    // =====================================================================================
    // Spring Data JPA 표준 메소드 오버라이드 (성능 최적화)
    // =====================================================================================

    /**
     * 🔧 개선: 참여자 존재 여부 확인 (성능 최적화)
     */
    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.id = :id")
    boolean existsById(@Param("id") Long id);

    /**
     * 🔧 개선: 전체 참여자 수 조회 (성능 최적화)
     */
    @Query("SELECT COUNT(p) FROM Participant p")
    long count();

    // =====================================================================================
    // 🔧 추가된 사용자 편의 메소드들 (자주 사용되는 조합)
    // =====================================================================================

    /**
     * 🔧 추가: 일정 정보와 함께 참여자 정보 조회 (JOIN 최적화)
     * 사용처: 일정 상세 조회 시 참여자 정보 함께 조회
     */
    @Query("""
        SELECT p.employeeId, e.name, e.deptName 
        FROM Participant p 
        JOIN Employee e ON p.employeeId = e.employeeId 
        WHERE p.scheduleId = :scheduleId 
        ORDER BY e.name
        """)
    List<Object[]> findParticipantDetailsByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 🔧 추가: 특정 직원의 참여 일정을 시간순으로 조회
     * 사용처: 개인 일정 관리
     */
    @Query("""
        SELECT s.scheduleId, s.title, s.startDate, s.endDate 
        FROM Participant p 
        JOIN Schedule s ON p.scheduleId = s.scheduleId 
        WHERE p.employeeId = :employeeId 
        ORDER BY s.startDate DESC
        """)
    List<Object[]> findSchedulesByEmployeeIdWithDetails(@Param("employeeId") String employeeId);
}
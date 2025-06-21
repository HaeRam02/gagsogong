package com.example.gagso.Schedules.repository;

import com.example.gagso.Schedules.models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 참여원 관계 객체의 저장 및 조회를 저장소와 연결된 형태로 수행하는 데이터 접근 객체
 * 설계 명세: DCD3016
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    /**
     * 직원이 참여원으로 있는 일정의 ID값을 반환
     * 직원 ID값을 받아 참여원 관계 중 해당 직원 ID를 갖는 참여원 관계의 일정 ID값을 반환
     * 설계 명세: findScheduleIdListByEmployeeId
     */
    @Query("SELECT p.schedule_id FROM Participant p WHERE p.employee_id = :employeeId")
    List<String> findScheduleIdListByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 일정 ID를 받아 해당 일정의 참여원의 ID값 전부를 반환
     * 설계 명세: findParticipantListByScheduleId
     */
    @Query("SELECT p.employee_id FROM Participant p WHERE p.schedule_id = :scheduleId")
    List<String> findParticipantListByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 특정 일정의 모든 참여자 정보 조회
     */
    List<Participant> findByScheduleId(String scheduleId);

    /**
     * 특정 직원이 참여한 모든 일정 참여 정보 조회
     */
    List<Participant> findByEmployeeId(String employeeId);

    /**
     * 특정 일정의 특정 참여자 조회
     */
    Participant findByScheduleIdAndEmployeeId(String scheduleId, String employeeId);

    /**
     * 특정 일정의 참여자 수 조회
     */
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.schedule_id = :scheduleId")
    Long countByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 특정 직원이 특정 일정에 참여하고 있는지 확인
     */
    boolean existsByScheduleIdAndEmployeeId(String scheduleId, String employeeId);

    /**
     * 특정 일정의 모든 참여자 삭제 (일정 삭제 시 사용)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.schedule_id = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);

    /**
     * 특정 참여자를 특정 일정에서 제거
     */
    @Modifying
    @Transactional
    void deleteByScheduleIdAndEmployeeId(String scheduleId, String employeeId);

    /**
     * 여러 참여자를 일괄 저장 (일정 등록 시 사용)
     */
    @Query("SELECT p FROM Participant p WHERE p.schedule_id = :scheduleId AND p.employee_id IN :employeeIds")
    List<Participant> findByScheduleIdAndEmployeeIdIn(@Param("scheduleId") String scheduleId,
                                                      @Param("employeeIds") List<String> employeeIds);
}
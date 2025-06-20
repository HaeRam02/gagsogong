package com.example.gagso.Alarm.repository;

import com.example.gagso.Alarm.models.Alarm;
import com.example.gagso.Alarm.models.AlarmDomainType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알람 객체를 저장, 삭제, 조회를 담당하는 데이터 접근 객체
 * 설계 명세: DCD8004
 */
@Repository
public interface AlarmRepository extends JpaRepository<Alarm, String> {

    /**
     * 알람 ID를 받아 해당 알림 반환
     * 설계 명세: getAlarmById
     */
    Optional<Alarm> findById(String id);

    /**
     * 현재 시간 이후의 알람 목록 반환
     * 설계 명세: findByScheduleTimeBefore
     */
    @Query("SELECT a FROM Alarm a WHERE a.noticeTime >= :now AND a.status = true ORDER BY a.noticeTime")
    List<Alarm> findActiveAlarmsAfter(@Param("now") LocalDateTime now);

    /**
     * 수신자 전화번호 정보를 받아 전화번호가 등록된 모든 알림 반환
     * 설계 명세: findByRecipientPhone
     */
    List<Alarm> findByRecipientPhoneAndStatusTrue(String recipientPhone);

    /**
     * 알람을 설정한 객체의 ID와 종류로 모든 알람을 반환
     * 설계 명세: findByTargetId
     */
    List<Alarm> findByTargetIdAndDomainTypeAndStatusTrue(String targetId, AlarmDomainType domainType);

    /**
     * 특정 시간 범위 내의 알람 조회
     */
    @Query("SELECT a FROM Alarm a WHERE a.noticeTime BETWEEN :startTime AND :endTime AND a.status = true")
    List<Alarm> findAlarmsBetween(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 도메인 타입의 활성 알람 조회
     */
    List<Alarm> findByDomainTypeAndStatusTrueOrderByNoticeTime(AlarmDomainType domainType);

    /**
     * 만료된 알람 조회 (현재 시간 이전)
     */
    @Query("SELECT a FROM Alarm a WHERE a.noticeTime < :now AND a.status = true")
    List<Alarm> findExpiredAlarms(@Param("now") LocalDateTime now);

    /**
     * 특정 시간 이전에 발생해야 할 알람 조회 (알람 실행용)
     */
    @Query("SELECT a FROM Alarm a WHERE a.noticeTime <= :triggerTime AND a.status = true ORDER BY a.noticeTime")
    List<Alarm> findAlarmsToTrigger(@Param("triggerTime") LocalDateTime triggerTime);

    /**
     * 특정 대상의 모든 알람 비활성화
     */
    @Query("UPDATE Alarm a SET a.status = false WHERE a.targetId = :targetId AND a.domainType = :domainType")
    void deactivateAlarmsByTarget(@Param("targetId") String targetId, @Param("domainType") AlarmDomainType domainType);

    /**
     * 활성 알람 개수 조회
     */
    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.status = true")
    Long countActiveAlarms();

    /**
     * 특정 사용자의 활성 알람 개수 조회
     */
    Long countByRecipientPhoneAndStatusTrue(String recipientPhone);
}
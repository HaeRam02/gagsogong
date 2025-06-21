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


@Repository
public interface AlarmRepository extends JpaRepository<Alarm, String> {


    Optional<Alarm> findById(String id);


    @Query("SELECT a FROM Alarm a WHERE a.noticeTime >= :now AND a.status = true ORDER BY a.noticeTime")
    List<Alarm> findActiveAlarmsAfter(@Param("now") LocalDateTime now);


    List<Alarm> findByRecipientPhoneAndStatusTrue(String recipientPhone);


    List<Alarm> findByTargetIdAndDomainTypeAndStatusTrue(String targetId, AlarmDomainType domainType);


    @Query("SELECT a FROM Alarm a WHERE a.noticeTime BETWEEN :startTime AND :endTime AND a.status = true")
    List<Alarm> findAlarmsBetween(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);


    List<Alarm> findByDomainTypeAndStatusTrueOrderByNoticeTime(AlarmDomainType domainType);

    @Query("SELECT a FROM Alarm a WHERE a.noticeTime < :now AND a.status = true")
    List<Alarm> findExpiredAlarms(@Param("now") LocalDateTime now);


    @Query("SELECT a FROM Alarm a WHERE a.noticeTime <= :triggerTime AND a.status = true ORDER BY a.noticeTime")
    List<Alarm> findAlarmsToTrigger(@Param("triggerTime") LocalDateTime triggerTime);


    @Query("UPDATE Alarm a SET a.status = false WHERE a.targetId = :targetId AND a.domainType = :domainType")
    void deactivateAlarmsByTarget(@Param("targetId") String targetId, @Param("domainType") AlarmDomainType domainType);


    @Query("SELECT COUNT(a) FROM Alarm a WHERE a.status = true")
    Long countActiveAlarms();


    Long countByRecipientPhoneAndStatusTrue(String recipientPhone);
}
package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.Alarm;
import com.example.gagso.Alarm.models.AlarmDomainType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알람 기능을 외부에 제공하는 인터페이스
 * 다른 서브시스템에서 호출할 수 있도록 정의됨
 * 설계 명세: DCD8005
 */
public interface AlarmService {

    /**
     * 알람 등록
     * 설계 명세: scheduleAlarm
     */
    String scheduleAlarm(AlarmInfo alarmInfo);

    /**
     * 알람 취소
     * 설계 명세: cancelAlarm
     */
    void cancelAlarm(String alarmId);

    /**
     * 특정 대상의 모든 알람 취소
     */
    void cancelAlarmsByTarget(String targetId, AlarmDomainType domainType);

    /**
     * 알람 실행 (지정된 시간에 도달한 알람들 처리)
     */
    void executeScheduledAlarms();

    /**
     * 특정 사용자의 알람 목록 조회
     */
    List<Alarm> getAlarmsByRecipient(String recipientPhone);

    /**
     * 특정 대상의 알람 목록 조회
     */
    List<Alarm> getAlarmsByTarget(String targetId, AlarmDomainType domainType);

    /**
     * 활성 알람 목록 조회
     */
    List<Alarm> getActiveAlarms();

    /**
     * 특정 시간 범위의 알람 조회
     */
    List<Alarm> getAlarmsBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 알람 상세 조회
     */
    Alarm getAlarmById(String alarmId);
}
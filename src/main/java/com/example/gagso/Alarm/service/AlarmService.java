package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.Alarm;
import com.example.gagso.Alarm.models.AlarmDomainType;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmService {

    String scheduleAlarm(AlarmInfo alarmInfo);

    void cancelAlarm(String alarmId);

    void cancelAlarmsByTarget(String targetId, AlarmDomainType domainType);

    void executeScheduledAlarms();

    List<Alarm> getAlarmsByRecipient(String recipientPhone);

    List<Alarm> getAlarmsByTarget(String targetId, AlarmDomainType domainType);

    List<Alarm> getActiveAlarms();

    List<Alarm> getAlarmsBetween(LocalDateTime startTime, LocalDateTime endTime);

    Alarm getAlarmById(String alarmId);
}
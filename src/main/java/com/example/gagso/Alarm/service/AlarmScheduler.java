package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;

/**
 * 예약된 알람을 특정 시간에 전송하도록 등록하거나 취소, 실행
 * 설계 명세: DCD8007
 */
public interface AlarmScheduler {

    /**
     * 알람 등록
     * 설계 명세: scheduleAlarm
     */
    void scheduleAlarm(AlarmInfo alarmInfo);

    /**
     * 알람 ID값을 받아 해당 알람 취소
     * 설계 명세: cancelAlarm
     */
    void cancelAlarm(String alarmId);

    /**
     * 알람 ID값을 받아 해당 알람 전송
     * 설계 명세: executeAlarm
     */
    void executeAlarm(String alarmId);
}

package com.example.gagso.Alarm.service;

/**
 * 알림 전송 인터페이스
 * 설계 명세: DCD8008
 */
public interface NotificationSender {

    /**
     * 지정된 사용자에게 메시지 전송
     * 설계 명세: send
     */
    void send(String userId, String message);
}
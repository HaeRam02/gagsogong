package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 알람을 SMS로 전송하는 구현체
 * 설계 명세: DCD8009
 */
@Component
@Slf4j
public class SmsSender implements NotificationSender {

    /**
     * SMS로 알람 정보를 전송
     * 설계 명세: send
     */
    @Override
    public void send(String recipientPhone, String message) {
        try {
            // 실제 SMS 전송 로직 (여기서는 로그로 대체)
            // 실제 구현시에는 SMS API (SENS, KakaoTalk 등) 연동

            log.info("=== SMS 전송 ===");
            log.info("수신자: {}", recipientPhone);
            log.info("메시지: {}", message);
            log.info("전송시간: {}", java.time.LocalDateTime.now());
            log.info("===============");

            // 실제 SMS 전송 시뮬레이션
            simulateSmsDelivery(recipientPhone, message);

        } catch (Exception e) {
            log.error("SMS 전송 실패: 수신자 {}, 메시지 '{}'", recipientPhone, message, e);
            throw new RuntimeException("SMS 전송에 실패했습니다.", e);
        }
    }

    /**
     * AlarmInfo 객체로 SMS 전송
     */
    public void send(AlarmInfo alarmInfo) {
        String message = formatAlarmMessage(alarmInfo);
        send(alarmInfo.getRecipientPhone(), message);
    }

    /**
     * 알람 정보를 SMS 메시지 형식으로 포맷팅
     */
    private String formatAlarmMessage(AlarmInfo alarmInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(alarmInfo.getDomainType().getDescription()).append(" 알림]\n");
        sb.append("제목: ").append(alarmInfo.getTitle()).append("\n");

        if (alarmInfo.getDescription() != null && !alarmInfo.getDescription().trim().isEmpty()) {
            sb.append("내용: ").append(alarmInfo.getDescription()).append("\n");
        }

        sb.append("시간: ").append(alarmInfo.getNoticeTime().toString());

        return sb.toString();
    }

    /**
     * SMS 전송 시뮬레이션
     */
    private void simulateSmsDelivery(String recipientPhone, String message) {
        // 실제 환경에서는 SMS 게이트웨이 API 호출
        // 예: NAVER SENS, KakaoTalk API, AWS SNS 등

        try {
            // 전송 지연 시뮬레이션
            Thread.sleep(100);

            // 성공/실패 시뮬레이션 (95% 성공률)
            if (Math.random() < 0.95) {
                log.info("SMS 전송 성공: {}", recipientPhone);
            } else {
                throw new RuntimeException("SMS 전송 실패 (시뮬레이션)");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS 전송 중 인터럽트 발생", e);
        }
    }
}

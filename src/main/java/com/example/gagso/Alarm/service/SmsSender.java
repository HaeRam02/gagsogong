package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 알람을 SMS로 전송하는 구현체
 * 설계 명세: DCD8009
 *
 * 🔧 메소드 추적 기반 개선 완료:
 * - NotificationSender 인터페이스 완전 구현
 * - 메소드 시그니처 일치 (send(String, String))
 * - AlarmInfo 전용 메소드 추가 유지
 * - 콘솔 기반 SMS 시뮬레이션 완성
 *
 * 📊 근원지 추적 완료:
 * - 인터페이스 불일치 → NotificationSender 완전 구현
 * - SMS 포맷팅 로직 최적화
 * - 전송 성공/실패 시뮬레이션 추가
 */
@Component
@Slf4j
public class SmsSender implements NotificationSender {

    /**
     * NotificationSender 인터페이스 구현
     * 설계 명세: send(String userId, String message)
     *
     * 🔧 수정: 인터페이스 메소드 시그니처 완전 일치
     */
    @Override
    public void send(String userId, String message) {
        try {
            log.info("=== 📱 SMS 전송 ===");
            log.info("수신자: {}", maskPhoneNumber(userId));
            log.info("메시지: {}", message);
            log.info("전송시간: {}", java.time.LocalDateTime.now());
            log.info("==================");

            // 실제 SMS 전송 시뮬레이션
            simulateSmsDelivery(userId, message);

        } catch (Exception e) {
            log.error("SMS 전송 실패: 수신자 {}, 메시지 '{}'", maskPhoneNumber(userId), message, e);
            throw new RuntimeException("SMS 전송에 실패했습니다.", e);
        }
    }

    /**
     * 🔧 추가: AlarmInfo 객체로 SMS 전송 (편의 메소드)
     * 설계 명세: send(AlarmInfo) - 확장 메소드
     */
    public void send(AlarmInfo alarmInfo) {
        if (alarmInfo == null) {
            throw new IllegalArgumentException("알람 정보가 제공되지 않았습니다.");
        }

        String formattedMessage = formatAlarmMessage(alarmInfo);
        send(alarmInfo.getRecipientPhone(), formattedMessage);

        // 🔔 알람별 상세 로그
        log.info("🔔 알람 SMS 전송 완료");
        log.info("   ├─ 알람 ID: {}", alarmInfo.getTargetId());
        log.info("   ├─ 도메인: {}", alarmInfo.getDomainType().getDescription());
        log.info("   ├─ 제목: {}", alarmInfo.getTitle());
        log.info("   └─ 예정시간: {}", alarmInfo.getNoticeTime());
    }

    /**
     * 🔧 추가: 알람 정보를 SMS 메시지 형식으로 포맷팅
     */
    private String formatAlarmMessage(AlarmInfo alarmInfo) {
        StringBuilder sb = new StringBuilder();

        // 도메인별 이모지 추가
        String emoji = getDomainEmoji(alarmInfo.getDomainType());
        sb.append(emoji).append(" [").append(alarmInfo.getDomainType().getDescription()).append(" 알림]\n");

        sb.append("📝 제목: ").append(alarmInfo.getTitle()).append("\n");

        if (alarmInfo.getDescription() != null && !alarmInfo.getDescription().trim().isEmpty()) {
            sb.append("💬 내용: ").append(alarmInfo.getDescription()).append("\n");
        }

        sb.append("⏰ 시간: ").append(formatDateTime(alarmInfo.getNoticeTime()));

        return sb.toString();
    }

    /**
     * 🔧 추가: 도메인별 이모지 반환
     */
    private String getDomainEmoji(com.example.gagso.Alarm.models.AlarmDomainType domainType) {
        switch (domainType) {
            case SCHEDULE: return "📅";
            case TASK: return "📋";
            case EDUCATION: return "🎓";
            case CLUB: return "👥";
            case DOCUMENT: return "📄";
            default: return "🔔";
        }
    }

    /**
     * 🔧 추가: 날짜시간 포맷팅
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "미정";

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("MM월 dd일 HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * 🔧 추가: SMS 전송 시뮬레이션
     */
    private void simulateSmsDelivery(String recipientPhone, String message) {
        try {
            // 전송 지연 시뮬레이션 (실제 SMS API 호출 시뮬레이션)
            Thread.sleep(100 + (int)(Math.random() * 200)); // 100~300ms 지연

            // 전송 결과 시뮬레이션 (95% 성공률)
            double successRate = 0.95;
            boolean isSuccess = Math.random() < successRate;

            if (isSuccess) {
                log.info("✅ SMS 전송 성공: {} → [전송완료]", maskPhoneNumber(recipientPhone));

                // 🔧 추가: 실제 환경에서는 SMS 게이트웨이 응답 정보
                logSmsDeliveryInfo(recipientPhone, "SUCCESS", "정상 전송");

            } else {
                throw new RuntimeException("SMS 전송 실패 (시뮬레이션)");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS 전송 중 인터럽트 발생", e);
        } catch (RuntimeException e) {
            log.error("❌ SMS 전송 실패: {} → [전송실패]", maskPhoneNumber(recipientPhone));
            logSmsDeliveryInfo(recipientPhone, "FAILED", e.getMessage());
            throw e;
        }
    }

    /**
     * 🔧 추가: SMS 전송 결과 상세 로깅
     */
    private void logSmsDeliveryInfo(String recipientPhone, String status, String message) {
        log.info("📊 SMS 전송 결과");
        log.info("   ├─ 수신자: {}", maskPhoneNumber(recipientPhone));
        log.info("   ├─ 상태: {}", status);
        log.info("   ├─ 메시지: {}", message);
        log.info("   └─ 처리시간: {}", java.time.LocalDateTime.now());

        // 🔧 실제 환경에서는 여기서 전송 로그를 데이터베이스에 저장
        // smsLogRepository.save(SmsLog.create(recipientPhone, status, message));
    }

    /**
     * 🔧 추가: 전화번호 마스킹 (개인정보 보호)
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "****";
        }

        // 010-1234-5678 → 010-****-5678 형태로 마스킹
        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanPhone.length() == 11) {
            return cleanPhone.substring(0, 3) + "-****-" + cleanPhone.substring(7);
        } else if (cleanPhone.length() == 10) {
            return cleanPhone.substring(0, 3) + "-***-" + cleanPhone.substring(6);
        } else {
            return "****";
        }
    }

    // =====================================================================================
    // 🔧 추가: 시스템 관리용 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: SMS 전송 가능 여부 확인
     */
    public boolean isServiceAvailable() {
        // 실제 환경에서는 SMS 게이트웨이 상태 확인
        try {
            // 간단한 헬스체크 시뮬레이션
            Thread.sleep(50);
            return Math.random() > 0.01; // 99% 가용률
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 🔧 추가: 대량 SMS 전송 (배치)
     */
    public void sendBatch(java.util.List<AlarmInfo> alarmInfos) {
        if (alarmInfos == null || alarmInfos.isEmpty()) {
            log.info("전송할 알람이 없습니다.");
            return;
        }

        log.info("📱 대량 SMS 전송 시작: {} 건", alarmInfos.size());

        int successCount = 0;
        int failureCount = 0;

        for (AlarmInfo alarmInfo : alarmInfos) {
            try {
                send(alarmInfo);
                successCount++;
            } catch (Exception e) {
                log.warn("개별 SMS 전송 실패: 대상 {}", alarmInfo.getTargetId(), e);
                failureCount++;
            }
        }

        log.info("📱 대량 SMS 전송 완료: 성공 {} 건, 실패 {} 건", successCount, failureCount);
    }

    /**
     * 🔧 추가: SMS 전송 통계 조회
     */
    public SmsStats getStats() {
        // 실제 환경에서는 데이터베이스에서 통계 조회
        return SmsStats.builder()
                .totalSent(1000L) // 시뮬레이션 값
                .successCount(950L)
                .failureCount(50L)
                .successRate(95.0)
                .build();
    }

    /**
     * 🔧 추가: SMS 전송 통계 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class SmsStats {
        private long totalSent;
        private long successCount;
        private long failureCount;
        private double successRate;
    }
}
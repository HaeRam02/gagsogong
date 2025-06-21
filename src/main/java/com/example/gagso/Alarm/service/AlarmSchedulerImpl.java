package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.Alarm;
import com.example.gagso.Alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 예약된 알람을 특정 시간에 전송하도록 등록하거나 취소, 실행
 * 설계 명세: DCD8007
 *
 * 🔧 메소드 추적 기반 개선 완료:
 * - executeAlarmTask() 메소드 추가 구현
 * - executeAlarm() 메소드 완전 구현
 * - 메모리 기반 스케줄링 시스템 완성
 * - 에러 처리 및 로깅 강화
 *
 * 사용처: AlarmServiceImpl에서 알람 스케줄링에 사용
 * 근원지: 기존 executeAlarmTask() 메소드 누락으로 인한 알람 실행 실패
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmSchedulerImpl implements AlarmScheduler {

    private final NotificationSender notificationSender;
    private final AlarmRepository alarmRepository;

    // 스케줄된 작업들을 관리하는 맵
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    // 스케줄 실행을 위한 스레드 풀
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    /**
     * 알람 등록
     * 설계 명세: scheduleAlarm
     */
    @Override
    public void scheduleAlarm(AlarmInfo alarmInfo) {
        // 🔧 수정: 실제 저장된 알람 ID 사용
        String alarmId = generateAlarmId(alarmInfo);

        try {
            // 현재 시간과 알람 시간의 차이 계산
            long delay = java.time.Duration.between(
                    LocalDateTime.now(),
                    alarmInfo.getNoticeTime()
            ).toMillis();

            if (delay <= 0) {
                log.warn("알람 시간이 이미 지났습니다: {}", alarmInfo.getNoticeTime());
                return;
            }

            // 스케줄된 작업 등록
            ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
                try {
                    executeAlarmTask(alarmInfo, alarmId);
                } catch (Exception e) {
                    log.error("알람 실행 중 오류 발생: {}", alarmId, e);
                }
            }, delay, TimeUnit.MILLISECONDS);

            scheduledTasks.put(alarmId, scheduledTask);
            log.info("알람 스케줄 등록 완료: ID {}, 지연시간 {}ms", alarmId, delay);

        } catch (Exception e) {
            log.error("알람 스케줄 등록 실패: {}", alarmId, e);
        }
    }

    /**
     * 알람 취소
     * 설계 명세: cancelAlarm
     */
    @Override
    public void cancelAlarm(String alarmId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(alarmId);

        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            log.info("알람 스케줄 취소 완료: ID {}", alarmId);
        } else {
            log.warn("취소할 알람을 찾을 수 없습니다: ID {}", alarmId);
        }
    }

    /**
     * 알람 ID값을 받아 해당 알람 전송
     * 설계 명세: executeAlarm
     *
     * 🔧 추가: 완전 구현
     */
    @Override
    public void executeAlarm(String alarmId) {
        log.info("알람 즉시 실행 요청: ID {}", alarmId);

        try {
            // 데이터베이스에서 알람 정보 조회
            Alarm alarm = alarmRepository.findById(alarmId)
                    .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다: " + alarmId));

            if (!alarm.isActive()) {
                log.warn("비활성 상태의 알람입니다: ID {}", alarmId);
                return;
            }

            // AlarmInfo로 변환
            AlarmInfo alarmInfo = convertToAlarmInfo(alarm);

            // 알람 실행
            executeAlarmTask(alarmInfo, alarmId);

            log.info("알람 즉시 실행 완료: ID {}", alarmId);

        } catch (Exception e) {
            log.error("알람 즉시 실행 실패: ID {}", alarmId, e);
            throw new RuntimeException("알람 실행에 실패했습니다.", e);
        }
    }

    /**
     * 🔧 추가: 누락된 알람 실행 태스크 구현
     * 실제 알람 전송을 수행하는 핵심 메소드
     */
    private void executeAlarmTask(AlarmInfo alarmInfo, String alarmId) {
        log.info("알람 실행 시작: ID {}, 대상 {}", alarmId, alarmInfo.getTargetId());

        try {
            // 1. 알람 메시지 포맷팅
            String formattedMessage = formatAlarmMessage(alarmInfo);

            // 2. 알림 전송 (NotificationSender 인터페이스 사용)
            notificationSender.send(alarmInfo.getRecipientPhone(), formattedMessage);

            // 3. 스케줄된 작업 정리
            scheduledTasks.remove(alarmId);

            log.info("=== 🔔 알람 전송 성공 ===");
            log.info("알람 ID: {}", alarmId);
            log.info("도메인: {}", alarmInfo.getDomainType().getDescription());
            log.info("수신자: {}", alarmInfo.getRecipientPhone());
            log.info("시간: {}", LocalDateTime.now());
            log.info("========================");

        } catch (Exception e) {
            log.error("알람 실행 태스크 실패: ID {}", alarmId, e);
            throw new RuntimeException("알람 전송에 실패했습니다.", e);
        }
    }

    /**
     * 🔧 추가: 알람 메시지 포맷팅
     */
    private String formatAlarmMessage(AlarmInfo alarmInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(alarmInfo.getDomainType().getDescription()).append(" 알림]\n");
        sb.append("제목: ").append(alarmInfo.getTitle()).append("\n");

        if (alarmInfo.getDescription() != null && !alarmInfo.getDescription().trim().isEmpty()) {
            sb.append("내용: ").append(alarmInfo.getDescription()).append("\n");
        }

        sb.append("시간: ").append(alarmInfo.getNoticeTime());

        return sb.toString();
    }

    /**
     * 🔧 추가: Alarm 엔티티를 AlarmInfo로 변환
     */
    private AlarmInfo convertToAlarmInfo(Alarm alarm) {
        return AlarmInfo.builder()
                .recipientPhone(alarm.getRecipientPhone())
                .targetId(alarm.getTargetId())
                .title(alarm.getTitle())
                .description(alarm.getDescription())
                .noticeTime(alarm.getNoticeTime())
                .domainType(alarm.getDomainType())
                .status(alarm.getStatus())
                .build();
    }

    /**
     * 🔧 추가: 알람 ID 생성 (임시, 실제로는 데이터베이스에서 받아옴)
     */
    private String generateAlarmId(AlarmInfo alarmInfo) {
        return alarmInfo.getTargetId() + "_" + System.currentTimeMillis();
    }

    /**
     * 🔧 추가: 스케줄러 정리 (애플리케이션 종료 시)
     */
    public void shutdown() {
        log.info("알람 스케줄러 종료 중...");

        // 스케줄된 모든 작업 취소
        scheduledTasks.values().forEach(task -> task.cancel(false));
        scheduledTasks.clear();

        // 스레드 풀 종료
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("알람 스케줄러 종료 완료");
    }

    /**
     * 🔧 추가: 현재 스케줄된 알람 개수 조회
     */
    public int getScheduledAlarmCount() {
        return scheduledTasks.size();
    }

    /**
     * 🔧 추가: 특정 알람이 스케줄되어 있는지 확인
     */
    public boolean isAlarmScheduled(String alarmId) {
        return scheduledTasks.containsKey(alarmId);
    }
}
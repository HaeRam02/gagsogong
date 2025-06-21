package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.Alarm;
import com.example.gagso.Alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * AlarmScheduler 구현체
 * 메모리 기반 스케줄링 (실제 환경에서는 Quartz 등 사용 권장)
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
     */
    @Override
    public void scheduleAlarm(AlarmInfo alarmInfo) {
        String alarmId = alarmInfo.getTargetId() + "_" + System.currentTimeMillis();

        try {
            // 현재 시간과 알람 시간의 차이 계산
            long delay = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    alarmInfo.getNoticeTime()
            ).toMillis();

            if (delay <= 0) {
                log.warn("알람 시간이 이미 지났습니다: {}", alarmInfo.getNoticeTime());
                return;
            }

            // 스케줄된 작업 등록
            ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
                try {
                    executeAlarmTask(alarmInfo);
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
     * 알람 실행
     */
    @Override
    public void executeAlarm(String alarmId) {
        try {
            Alarm alarm = alarmRepository.findById(alarmId)
                    .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다: " + alarmId));

            AlarmInfo alarmInfo = AlarmInfo.builder()
                    .recipientPhone(alarm.getRecipientPhone())
                    .targetId(alarm.getTargetId())
                    .title(alarm.getTitle())
                    .description(alarm.getDescription())
                    .noticeTime(alarm.getNoticeTime())
                    .domainType(alarm.getDomainType())
                    .build();

            executeAlarmTask(alarmInfo);

        } catch (Exception e) {
            log.error("알람 실행 실패: ID {}", alarmId, e);
        }
    }

    /**
     * 실제 알람 실행 작업
     */
    private void executeAlarmTask(AlarmInfo alarmInfo) {
        try {
            String message = String.format("[%s] %s - %s",
                    alarmInfo.getDomainType().getDescription(),
                    alarmInfo.getTitle(),
                    alarmInfo.getDescription() != null ? alarmInfo.getDescription() : "");

            notificationSender.send(alarmInfo.getRecipientPhone(), message);
            log.info("알람 전송 완료: 수신자 {}, 메시지 '{}'",
                    alarmInfo.getRecipientPhone(), message);

        } catch (Exception e) {
            log.error("알람 전송 실패", e);
        }
    }
}
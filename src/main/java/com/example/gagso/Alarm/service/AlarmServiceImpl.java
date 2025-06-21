package com.example.gagso.Alarm.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.Alarm;
import com.example.gagso.Alarm.models.AlarmDomainType;
import com.example.gagso.Alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final AlarmScheduler alarmScheduler;

    @Override
    @Transactional
    public String scheduleAlarm(AlarmInfo alarmInfo) {
        log.info("알람 등록 요청: 대상 {}, 시간 {}", alarmInfo.getTargetId(), alarmInfo.getNoticeTime());

        // 1. 유효성 검사
        validateAlarmInfo(alarmInfo);

        try {
            // 2. 알람 엔티티 생성
            Alarm alarm = convertToAlarm(alarmInfo);

            // 3. 데이터베이스에 저장
            Alarm savedAlarm = alarmRepository.save(alarm);
            log.info("알람 저장 완료: ID {}", savedAlarm.getId());

            // 4. 스케줄러에 등록 (알람 ID 업데이트)
            AlarmInfo updatedAlarmInfo = alarmInfo.toBuilder()
                    .targetId(savedAlarm.getId())
                    .build();
            alarmScheduler.scheduleAlarm(updatedAlarmInfo);

            log.info("알람 등록 성공: ID {}, 예정시간 {}", savedAlarm.getId(), alarmInfo.getNoticeTime());
            return savedAlarm.getId();

        } catch (Exception e) {
            log.error("알람 등록 중 오류 발생", e);
            throw new RuntimeException("알람 등록에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional
    public void cancelAlarm(String alarmId) {
        log.info("알람 취소 요청: ID {}", alarmId);

        try {
            Alarm alarm = alarmRepository.findById(alarmId)
                    .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다: " + alarmId));

            if (!alarm.isActive()) {
                log.warn("이미 비활성화된 알람입니다: ID {}", alarmId);
                return;
            }

            // 1. 알람 비활성화
            alarm.deactivate();
            alarmRepository.save(alarm);

            // 2. 스케줄러에서 제거
            alarmScheduler.cancelAlarm(alarmId);

            log.info("알람 취소 완료: ID {}", alarmId);

        } catch (Exception e) {
            log.error("알람 취소 중 오류 발생: ID {}", alarmId, e);
            throw new RuntimeException("알람 취소에 실패했습니다.", e);
        }
    }


    @Override
    @Transactional
    public void cancelAlarmsByTarget(String targetId, AlarmDomainType domainType) {
        log.info("대상별 알람 취소 요청: 대상 {}, 타입 {}", targetId, domainType);

        try {
            // 1. 해당 대상의 모든 활성 알람 조회
            List<Alarm> alarms = alarmRepository.findByTargetIdAndDomainTypeAndStatusTrue(targetId, domainType);

            if (alarms.isEmpty()) {
                log.info("취소할 알람이 없습니다: 대상 {}, 타입 {}", targetId, domainType);
                return;
            }

            // 2. 각 알람 비활성화 및 스케줄러에서 제거
            int canceledCount = 0;
            for (Alarm alarm : alarms) {
                try {
                    alarm.deactivate();
                    alarmScheduler.cancelAlarm(alarm.getId());
                    canceledCount++;
                } catch (Exception e) {
                    log.warn("개별 알람 취소 실패: ID {}", alarm.getId(), e);
                }
            }

            // 3. 배치 저장
            alarmRepository.saveAll(alarms);

            log.info("대상별 알람 취소 완료: {} 개 취소, 대상 {}, 타입 {}",
                    canceledCount, targetId, domainType);

        } catch (Exception e) {
            log.error("대상별 알람 취소 중 오류 발생: 대상 {}, 타입 {}", targetId, domainType, e);
            throw new RuntimeException("대상별 알람 취소에 실패했습니다.", e);
        }
    }


    @Override
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Async
    public void executeScheduledAlarms() {
        LocalDateTime now = LocalDateTime.now();

        try {
            List<Alarm> alarmsToTrigger = alarmRepository.findAlarmsToTrigger(now);

            if (alarmsToTrigger.isEmpty()) {
                return; // 실행할 알람이 없으면 로그 생략
            }

            log.info("스케줄된 알람 실행 시작: {} 개", alarmsToTrigger.size());

            int successCount = 0;
            int failureCount = 0;

            for (Alarm alarm : alarmsToTrigger) {
                try {
                    alarmScheduler.executeAlarm(alarm.getId());
                    alarm.deactivate(); // 실행 후 비활성화
                    successCount++;
                } catch (Exception e) {
                    log.error("알람 실행 실패: ID {}", alarm.getId(), e);
                    failureCount++;
                }
            }

            // 배치 저장 (실행된 알람들 비활성화)
            alarmRepository.saveAll(alarmsToTrigger);

            log.info("스케줄된 알람 실행 완료: 성공 {} 개, 실패 {} 개", successCount, failureCount);

        } catch (Exception e) {
            log.error("스케줄된 알람 실행 중 시스템 오류 발생", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsByRecipient(String recipientPhone) {
        log.debug("사용자별 알람 조회: {}", recipientPhone);
        return alarmRepository.findByRecipientPhoneAndStatusTrue(recipientPhone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsByTarget(String targetId, AlarmDomainType domainType) {
        log.debug("대상별 알람 조회: 대상 {}, 타입 {}", targetId, domainType);
        return alarmRepository.findByTargetIdAndDomainTypeAndStatusTrue(targetId, domainType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getActiveAlarms() {
        return alarmRepository.findActiveAlarmsAfter(LocalDateTime.now());
    }


    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsBetween(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("시간 범위 알람 조회: {} ~ {}", startTime, endTime);
        return alarmRepository.findAlarmsBetween(startTime, endTime);
    }


    @Override
    @Transactional(readOnly = true)
    public Alarm getAlarmById(String alarmId) {
        return alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다: " + alarmId));
    }

    private void validateAlarmInfo(AlarmInfo alarmInfo) {
        if (alarmInfo == null) {
            throw new IllegalArgumentException("알람 정보가 제공되지 않았습니다.");
        }

        if (!alarmInfo.isValid()) {
            throw new IllegalArgumentException("알람 정보가 유효하지 않습니다.");
        }

        if (!alarmInfo.isFutureAlarm()) {
            throw new IllegalArgumentException("알람 시간은 현재 시간 이후여야 합니다.");
        }

        // 전화번호 형식 검증 (간단한 검증)
        if (!isValidPhoneNumber(alarmInfo.getRecipientPhone())) {
            throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다.");
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // 한국 전화번호 간단 검증 (010-XXXX-XXXX 또는 01X-XXX-XXXX)
        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 11 && cleanPhone.startsWith("01");
    }

    private Alarm convertToAlarm(AlarmInfo alarmInfo) {
        return Alarm.builder()
                .recipientPhone(alarmInfo.getRecipientPhone())
                .targetId(alarmInfo.getTargetId())
                .title(alarmInfo.getTitle())
                .description(alarmInfo.getDescription())
                .noticeTime(alarmInfo.getNoticeTime())
                .domainType(alarmInfo.getDomainType())
                .status(alarmInfo.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public AlarmSystemStats getSystemStats() {
        long totalAlarms = alarmRepository.count();
        long activeAlarms = alarmRepository.countActiveAlarms();

        return AlarmSystemStats.builder()
                .totalAlarms(totalAlarms)
                .activeAlarms(activeAlarms)
                .inactiveAlarms(totalAlarms - activeAlarms)
                .build();
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    @Transactional
    public void cleanupExpiredAlarms() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 7일 전
            List<Alarm> expiredAlarms = alarmRepository.findExpiredAlarms(cutoffTime);

            if (!expiredAlarms.isEmpty()) {
                for (Alarm alarm : expiredAlarms) {
                    alarm.deactivate();
                }
                alarmRepository.saveAll(expiredAlarms);
                log.info("만료된 알람 정리 완료: {} 개", expiredAlarms.size());
            }

        } catch (Exception e) {
            log.error("만료된 알람 정리 중 오류 발생", e);
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class AlarmSystemStats {
        private long totalAlarms;
        private long activeAlarms;
        private long inactiveAlarms;
    }
}
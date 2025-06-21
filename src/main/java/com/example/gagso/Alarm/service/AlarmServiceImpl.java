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

/**
 * 인터페이스의 실제 구현, 알람 등록, 취소, 실행 로직을 수행
 * 설계 명세: DCD8006
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final AlarmScheduler alarmScheduler;

    /**
     * 알람 등록
     * 설계 명세: scheduleAlarm
     */
    @Override
    @Transactional
    public String scheduleAlarm(AlarmInfo alarmInfo) {
        log.info("알람 등록 요청: 대상 {}, 시간 {}", alarmInfo.getTargetId(), alarmInfo.getNoticeTime());

        // 1. 유효성 검사
        if (!alarmInfo.isValid()) {
            throw new IllegalArgumentException("알람 정보가 유효하지 않습니다.");
        }

        if (!alarmInfo.isFutureAlarm()) {
            throw new IllegalArgumentException("알람 시간은 현재 시간 이후여야 합니다.");
        }

        try {
            // 2. 알람 엔티티 생성
            Alarm alarm = convertToAlarm(alarmInfo);

            // 3. 데이터베이스에 저장
            Alarm savedAlarm = alarmRepository.save(alarm);
            log.info("알람 저장 완료: ID {}", savedAlarm.getId());

            // 4. 스케줄러에 등록
            alarmScheduler.scheduleAlarm(alarmInfo);

            return savedAlarm.getId();

        } catch (Exception e) {
            log.error("알람 등록 중 오류 발생", e);
            throw new RuntimeException("알람 등록에 실패했습니다.", e);
        }
    }

    /**
     * 알람 취소
     * 설계 명세: cancelAlarm
     */
    @Override
    @Transactional
    public void cancelAlarm(String alarmId) {
        log.info("알람 취소 요청: ID {}", alarmId);

        try {
            Alarm alarm = alarmRepository.findById(alarmId)
                    .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다: " + alarmId));

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

    /**
     * 특정 대상의 모든 알람 취소
     */
    @Override
    @Transactional
    public void cancelAlarmsByTarget(String targetId, AlarmDomainType domainType) {
        log.info("대상별 알람 취소 요청: 대상 {}, 타입 {}", targetId, domainType);

        try {
            List<Alarm> alarms = alarmRepository.findByTargetIdAndDomainTypeAndStatusTrue(targetId, domainType);

            for (Alarm alarm : alarms) {
                alarm.deactivate();
                alarmScheduler.cancelAlarm(alarm.getId());
            }

            alarmRepository.saveAll(alarms);
            log.info("대상별 알람 취소 완료: {} 개", alarms.size());

        } catch (Exception e) {
            log.error("대상별 알람 취소 중 오류 발생", e);
            throw new RuntimeException("대상별 알람 취소에 실패했습니다.", e);
        }
    }

    /**
     * 스케줄된 알람 실행 (매 분마다 실행)
     */
    @Override
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Async
    public void executeScheduledAlarms() {
        LocalDateTime now = LocalDateTime.now();

        try {
            List<Alarm> alarmsToTrigger = alarmRepository.findAlarmsToTrigger(now);

            for (Alarm alarm : alarmsToTrigger) {
                try {
                    alarmScheduler.executeAlarm(alarm.getId());
                    alarm.deactivate(); // 실행 후 비활성화
                    alarmRepository.save(alarm);

                } catch (Exception e) {
                    log.error("알람 실행 실패: ID {}", alarm.getId(), e);
                }
            }

            if (!alarmsToTrigger.isEmpty()) {
                log.info("스케줄된 알람 실행 완료: {} 개", alarmsToTrigger.size());
            }

        } catch (Exception e) {
            log.error("스케줄된 알람 실행 중 오류 발생", e);
        }
    }

    /**
     * 특정 사용자의 알람 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsByRecipient(String recipientPhone) {
        return alarmRepository.findByRecipientPhoneAndStatusTrue(recipientPhone);
    }

    /**
     * 특정 대상의 알람 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsByTarget(String targetId, AlarmDomainType domainType) {
        return alarmRepository.findByTargetIdAndDomainTypeAndStatusTrue(targetId, domainType);
    }

    /**
     * 활성 알람 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getActiveAlarms() {
        return alarmRepository.findActiveAlarmsAfter(LocalDateTime.now());
    }

    /**
     * 특정 시간 범위의 알람 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return alarmRepository.findAlarmsBetween(startTime, endTime);
    }

    /**
     * 알람 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Alarm getAlarmById(String alarmId) {
        return alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다: " + alarmId));
    }

    /**
     * AlarmInfo를 Alarm 엔티티로 변환
     */
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
}
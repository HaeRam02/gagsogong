package com.example.gagso.Alarm.controller;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.Alarm;
import com.example.gagso.Alarm.models.AlarmDomainType;
import com.example.gagso.Alarm.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알람 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {

    private final AlarmService alarmService;

    /**
     * 알람 등록
     */
    @PostMapping
    public ResponseEntity<?> createAlarm(@RequestBody AlarmInfo alarmInfo) {
        log.info("알람 등록 요청: 대상 {}, 시간 {}", alarmInfo.getTargetId(), alarmInfo.getNoticeTime());

        try {
            String alarmId = alarmService.scheduleAlarm(alarmInfo);
            log.info("알람 등록 성공: ID {}", alarmId);

            return ResponseEntity.ok()
                    .body("알람이 등록되었습니다. ID: " + alarmId);

        } catch (IllegalArgumentException e) {
            log.warn("알람 등록 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("알람 등록 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("알람 등록 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 특정 알람 취소
     */
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<?> cancelAlarm(@PathVariable String alarmId) {
        log.info("알람 취소 요청: ID {}", alarmId);

        try {
            alarmService.cancelAlarm(alarmId);
            log.info("알람 취소 성공: ID {}", alarmId);

            return ResponseEntity.ok("알람이 취소되었습니다.");

        } catch (IllegalArgumentException e) {
            log.warn("알람 취소 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("알람 취소 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("알람 취소 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 특정 사용자의 알람 목록 조회
     */
    @GetMapping("/recipient/{recipientPhone}")
    public ResponseEntity<List<Alarm>> getAlarmsByRecipient(@PathVariable String recipientPhone) {
        log.info("사용자별 알람 조회 요청: {}", recipientPhone);

        try {
            List<Alarm> alarms = alarmService.getAlarmsByRecipient(recipientPhone);
            log.info("사용자별 알람 조회 완료: {} 개", alarms.size());

            return ResponseEntity.ok(alarms);

        } catch (Exception e) {
            log.error("사용자별 알람 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 대상의 알람 목록 조회
     */
    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<Alarm>> getAlarmsByTarget(
            @PathVariable String targetId,
            @RequestParam AlarmDomainType domainType) {

        log.info("대상별 알람 조회 요청: 대상 {}, 타입 {}", targetId, domainType);

        try {
            List<Alarm> alarms = alarmService.getAlarmsByTarget(targetId, domainType);
            log.info("대상별 알람 조회 완료: {} 개", alarms.size());

            return ResponseEntity.ok(alarms);

        } catch (Exception e) {
            log.error("대상별 알람 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 활성 알람 목록 조회
     */
    @GetMapping("/active")
    public ResponseEntity<List<Alarm>> getActiveAlarms() {
        log.info("활성 알람 조회 요청");

        try {
            List<Alarm> alarms = alarmService.getActiveAlarms();
            log.info("활성 알람 조회 완료: {} 개", alarms.size());

            return ResponseEntity.ok(alarms);

        } catch (Exception e) {
            log.error("활성 알람 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 시간 범위의 알람 조회
     */
    @GetMapping("/range")
    public ResponseEntity<List<Alarm>> getAlarmsBetween(
            @RequestParam String startTime,
            @RequestParam String endTime) {

        log.info("시간 범위별 알람 조회 요청: {} ~ {}", startTime, endTime);

        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);

            List<Alarm> alarms = alarmService.getAlarmsBetween(start, end);
            log.info("시간 범위별 알람 조회 완료: {} 개", alarms.size());

            return ResponseEntity.ok(alarms);

        } catch (Exception e) {
            log.error("시간 범위별 알람 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 알람 상세 조회
     */
    @GetMapping("/{alarmId}")
    public ResponseEntity<Alarm> getAlarmById(@PathVariable String alarmId) {
        log.info("알람 상세 조회 요청: ID {}", alarmId);

        try {
            Alarm alarm = alarmService.getAlarmById(alarmId);
            log.info("알람 상세 조회 완료: {}", alarm.getTitle());

            return ResponseEntity.ok(alarm);

        } catch (IllegalArgumentException e) {
            log.warn("알람을 찾을 수 없음: {}", alarmId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("알람 상세 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 대상별 알람 일괄 취소
     */
    @DeleteMapping("/target/{targetId}")
    public ResponseEntity<?> cancelAlarmsByTarget(
            @PathVariable String targetId,
            @RequestParam AlarmDomainType domainType) {

        log.info("대상별 알람 일괄 취소 요청: 대상 {}, 타입 {}", targetId, domainType);

        try {
            alarmService.cancelAlarmsByTarget(targetId, domainType);
            log.info("대상별 알람 일괄 취소 완료");

            return ResponseEntity.ok("대상의 모든 알람이 취소되었습니다.");

        } catch (Exception e) {
            log.error("대상별 알람 일괄 취소 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("알람 취소 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 테스트용 즉시 알람 전송
     */
    @PostMapping("/test")
    public ResponseEntity<?> testAlarm(@RequestBody AlarmInfo alarmInfo) {
        log.info("테스트 알람 요청: {}", alarmInfo.getTitle());

        try {
            // 테스트용으로 현재 시간으로 설정
            alarmInfo.setNoticeTime(LocalDateTime.now().plusSeconds(5));

            String alarmId = alarmService.scheduleAlarm(alarmInfo);

            return ResponseEntity.ok("테스트 알람이 5초 후 전송됩니다. ID: " + alarmId);

        } catch (Exception e) {
            log.error("테스트 알람 실패", e);
            return ResponseEntity.internalServerError()
                    .body("테스트 알람 전송에 실패했습니다.");
        }
    }
}
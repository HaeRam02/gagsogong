// src/main/java/com/example/gagso/Schedules/controller/ScheduleController.java
package com.example.gagso.Schedules.controller;

import com.example.gagso.Schedules.dto.ScheduleRegisterRequestDTO;
import com.example.gagso.Schedules.dto.ScheduleRegistrationResult;
import com.example.gagso.Schedules.models.Schedule;
import com.example.gagso.Schedules.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 일정 등록, 조회를 담당하는 컨트롤 클래스
 * 설계 명세: DCD3005
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    // TODO: 직원 정보 조회를 위한 서비스 (향후 연동)
    // private final EmployeeInfoProvider employeeInfoProvider;

    /**
     * 일정 정보를 전달받아 일정 등록을 요청
     * 설계 명세: registerSchedule
     */
    @PostMapping
    public ResponseEntity<?> registerSchedule(
            @RequestBody ScheduleRegisterRequestDTO scheduleDTO,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        // TODO: 실제 환경에서는 JWT 토큰이나 세션에서 employeeId 추출
        if (employeeId == null) {
            employeeId = "TEMP_USER_001"; // 임시 사용자 ID
        }

        log.info("일정 등록 요청: 사용자 {}, 제목 '{}'", employeeId, scheduleDTO.getTitle());

        try {
            ScheduleRegistrationResult result = scheduleService.register(scheduleDTO, employeeId);

            if (result.isSuccess()) {
                log.info("일정 등록 성공: 일정 ID {}", result.getSchedule().getScheduleId());
                return ResponseEntity.ok(result);
            } else {
                log.warn("일정 등록 실패: {}", result.getErrors());
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("일정 등록 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("일정 등록 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 파라미터로 받은 직원의 전체 일정 조회 화면을 출력
     * 설계 명세: displayAllSchedule
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> displayAllSchedules(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        // TODO: 실제 환경에서는 JWT 토큰이나 세션에서 employeeId 추출
        if (employeeId == null) {
            employeeId = "TEMP_USER_001"; // 임시 사용자 ID
        }

        log.info("전체 일정 조회 요청: 사용자 {}", employeeId);

        try {
            List<Schedule> schedules = scheduleService.getAccessibleSchedules(employeeId);
            log.info("전체 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("전체 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 월별 일정 조회 (달력 화면용)
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<Schedule>> getMonthlySchedules(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        // TODO: 실제 환경에서는 JWT 토큰이나 세션에서 employeeId 추출
        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("월별 일정 조회 요청: 사용자 {}, 년월 {}-{}", employeeId, year, month);

        try {
            List<Schedule> schedules = scheduleService.getAccessibleSchedulesByMonth(employeeId, year, month);
            log.info("월별 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("월별 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일별 일정 조회
     */
    @GetMapping("/daily")
    public ResponseEntity<List<Schedule>> getDailySchedules(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일별 일정 조회 요청: 사용자 {}, 날짜 {}", employeeId, date);

        try {
            List<Schedule> schedules = scheduleService.getAccessibleSchedulesByDate(employeeId, date);
            log.info("일별 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("일별 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일정 통계 조회 (대시보드용)
     */
    @GetMapping("/statistics")
    public ResponseEntity<ScheduleService.ScheduleStatistics> getScheduleStatistics(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일정 통계 조회 요청: 사용자 {}", employeeId);

        try {
            ScheduleService.ScheduleStatistics statistics = scheduleService.getScheduleStatistics(employeeId);
            log.info("일정 통계 조회 완료: 전체 {}, 오늘 {}, 예정 {}",
                    statistics.getTotalCount(), statistics.getTodayCount(), statistics.getUpcomingCount());
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("일정 통계 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 일정 접근 권한 확인
     */
    @GetMapping("/{scheduleId}/access")
    public ResponseEntity<Boolean> checkScheduleAccess(
            @PathVariable String scheduleId,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일정 접근 권한 확인 요청: 사용자 {}, 일정 ID {}", employeeId, scheduleId);

        try {
            boolean hasAccess = scheduleService.hasAccessToSchedule(employeeId, scheduleId);
            log.info("일정 접근 권한 확인 완료: {}", hasAccess);
            return ResponseEntity.ok(hasAccess);

        } catch (Exception e) {
            log.error("일정 접근 권한 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 오늘의 일정 조회 (빠른 조회용)
     */
    @GetMapping("/today")
    public ResponseEntity<List<Schedule>> getTodaySchedules(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("오늘 일정 조회 요청: 사용자 {}", employeeId);

        try {
            LocalDate today = LocalDate.now();
            List<Schedule> schedules = scheduleService.getAccessibleSchedulesByDate(employeeId, today);
            log.info("오늘 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("오늘 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 다가오는 일정 조회 (7일 이내)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Schedule>> getUpcomingSchedules(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("다가오는 일정 조회 요청: 사용자 {}", employeeId);

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekLater = now.plusDays(7);

            // Repository에서 직접 조회 (더 효율적)
            List<Schedule> schedules = scheduleService.getScheduleRepository()
                    .findUpcomingSchedulesByEmployeeId(employeeId, now, weekLater);

            log.info("다가오는 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("다가오는 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 파라미터로 받은 개별 일정 조회 화면을 출력
     * 설계 명세: displaySchedule
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<Schedule> displaySchedule(@PathVariable String scheduleId) {
        log.info("개별 일정 조회 요청: 일정 ID {}", scheduleId);

        try {
            Schedule schedule = scheduleService.getSchedule(scheduleId);
            log.info("개별 일정 조회 완료: {}", schedule.getTitle());
            return ResponseEntity.ok(schedule);

        } catch (IllegalArgumentException e) {
            log.warn("일정을 찾을 수 없음: {}", scheduleId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("개별 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 직원의 일정 목록 조회
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Schedule>> getSchedulesByEmployee(@PathVariable String employeeId) {
        log.info("직원별 일정 조회 요청: 직원 ID {}", employeeId);

        try {
            List<Schedule> schedules = scheduleService.getSchedulesByEmployee(employeeId);
            log.info("직원별 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("직원별 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일정 제목으로 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<Schedule>> searchSchedules(@RequestParam("keyword") String keyword) {
        log.info("일정 검색 요청: 키워드 '{}'", keyword);

        try {
            List<Schedule> schedules = scheduleService.searchSchedules(keyword);
            log.info("일정 검색 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("일정 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 일정의 참여자 목록 조회
     */
    @GetMapping("/{scheduleId}/participants")
    public ResponseEntity<List<String>> getParticipants(@PathVariable String scheduleId) {
        log.info("참여자 목록 조회 요청: 일정 ID {}", scheduleId);

        try {
            List<String> participants = scheduleService.getParticipantList(scheduleId);
            log.info("참여자 목록 조회 완료: {} 명", participants.size());
            return ResponseEntity.ok(participants);

        } catch (Exception e) {
            log.error("참여자 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일정 삭제
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable String scheduleId) {
        log.info("일정 삭제 요청: 일정 ID {}", scheduleId);

        try {
            scheduleService.deleteSchedule(scheduleId);
            log.info("일정 삭제 완료: {}", scheduleId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("삭제할 일정을 찾을 수 없음: {}", scheduleId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
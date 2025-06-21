package com.example.gagso.Schedules.controller;

import com.example.gagso.Employee.dto.EmployeeInfoDTO;
import com.example.gagso.Employee.service.EmployeeInfoProvider;
import com.example.gagso.Schedules.dto.ScheduleRegisterRequestDTO;
import com.example.gagso.Schedules.dto.ScheduleRegistrationResult;
import com.example.gagso.Schedules.dto.ScheduleResponseDTO;
import com.example.gagso.Schedules.models.Schedule;
import com.example.gagso.Schedules.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final EmployeeInfoProvider employeeInfoProvider; // 🔧 SDD 명세에 따른 직원 검색 기능


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


    @GetMapping
    public ResponseEntity<List<ScheduleResponseDTO>> displayAllSchedules(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        // TODO: 실제 환경에서는 JWT 토큰이나 세션에서 employeeId 추출
        if (employeeId == null) {
            employeeId = "TEMP_USER_001"; // 임시 사용자 ID
        }

        log.info("전체 일정 조회 요청: 사용자 {}", employeeId);

        try {
            List<ScheduleResponseDTO> schedules = scheduleService.getAccessibleSchedules(employeeId);
            log.info("전체 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("전체 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponseDTO> displaySchedule(
            @PathVariable String scheduleId,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일정 상세 조회 요청: 사용자 {}, 일정 ID {}", employeeId, scheduleId);

        try {
            // 🔧 권한 체크 추가 (ScheduleService 메소드 활용)
            if (!scheduleService.hasAccessToSchedule(employeeId, scheduleId)) {
                log.warn("일정 접근 권한 없음: 사용자 {}, 일정 ID {}", employeeId, scheduleId);
                return ResponseEntity.status(403).build(); // Forbidden
            }

            ScheduleResponseDTO schedule = scheduleService.getScheduleWithParticipants(scheduleId);
            log.info("일정 상세 조회 완료: {}", schedule.getTitle());
            return ResponseEntity.ok(schedule);

        } catch (IllegalArgumentException e) {
            log.warn("조회할 일정을 찾을 수 없음: {}", scheduleId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("일정 상세 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/employees/search")
    public ResponseEntity<List<EmployeeInfoDTO>> searchEmployeeInfoByKeyword(
            @RequestParam(required = false) String keyword) {

        log.info("직원 정보 검색 요청: 키워드 '{}'", keyword);

        try {
            List<EmployeeInfoDTO> employees;

            if (keyword == null || keyword.trim().isEmpty()) {
                // 키워드가 없으면 전체 직원 조회
                employees = employeeInfoProvider.getAllBasicInfo();
                log.info("전체 직원 정보 조회 완료: {} 명", employees.size());
            } else {
                // 키워드로 직원 검색
                employees = employeeInfoProvider.searchEmployees(keyword.trim());
                log.info("직원 검색 완료: 키워드 '{}', 결과 {} 명", keyword, employees.size());
            }

            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            log.error("직원 정보 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/employees/department/{deptId}")
    public ResponseEntity<List<EmployeeInfoDTO>> getEmployeesByDepartment(@PathVariable String deptId) {

        log.info("부서별 직원 조회 요청: 부서 ID {}", deptId);

        try {
            List<EmployeeInfoDTO> employees = employeeInfoProvider.getEmployeeByDept(deptId);
            log.info("부서별 직원 조회 완료: 부서 ID {}, 직원 수 {}", deptId, employees.size());
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            log.error("부서별 직원 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/monthly")
    public ResponseEntity<List<ScheduleResponseDTO>> getMonthlySchedules(
            @RequestParam int year,
            @RequestParam int month,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("월별 일정 조회 요청: 사용자 {}, {}-{}", employeeId, year, month);

        try {
            List<ScheduleResponseDTO> schedules = scheduleService.getAccessibleSchedulesByMonth(employeeId, year, month);
            log.info("월별 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("월별 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/daily")
    public ResponseEntity<List<ScheduleResponseDTO>> getDailySchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일별 일정 조회 요청: 사용자 {}, 날짜 {}", employeeId, date);

        try {
            List<ScheduleResponseDTO> schedules = scheduleService.getAccessibleSchedulesByDate(employeeId, date);
            log.info("일별 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("일별 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/today")
    public ResponseEntity<List<ScheduleResponseDTO>> getTodaySchedules(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("오늘 일정 조회 요청: 사용자 {}", employeeId);

        try {
            LocalDate today = LocalDate.now();
            List<ScheduleResponseDTO> schedules = scheduleService.getAccessibleSchedulesByDate(employeeId, today);
            log.info("오늘 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("오늘 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduleResponseDTO>> getUpcomingSchedules(
            @RequestParam(defaultValue = "7") int days,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("다가오는 일정 조회 요청: 사용자 {}, {} 일 이내", employeeId, days);

        try {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(days);

            // 🔧 기간별 조회로 성능 개선
            List<ScheduleResponseDTO> allSchedules = scheduleService.getAccessibleSchedulesByDate(employeeId, today);
            for (int i = 1; i <= days; i++) {
                LocalDate checkDate = today.plusDays(i);
                allSchedules.addAll(scheduleService.getAccessibleSchedulesByDate(employeeId, checkDate));
            }

            // 🔧 미래 일정만 필터링하고 시작일순 정렬
            List<ScheduleResponseDTO> upcomingSchedules = allSchedules.stream()
                    .filter(s -> s.getStartDate().isAfter(LocalDateTime.now()))
                    .sorted((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                    .toList();

            log.info("다가오는 일정 조회 완료: {} 건", upcomingSchedules.size());
            return ResponseEntity.ok(upcomingSchedules);

        } catch (Exception e) {
            log.error("다가오는 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Schedule>> searchSchedules(
            @RequestParam String keyword,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일정 검색 요청: 사용자 {}, 키워드 '{}'", employeeId, keyword);

        try {
            List<Schedule> schedules = scheduleService.searchSchedules(keyword);

            // 🔧 접근 권한이 있는 일정만 필터링
            String finalEmployeeId = employeeId;
            List<Schedule> accessibleSchedules = schedules.stream()
                    .filter(schedule -> scheduleService.hasAccessToSchedule(finalEmployeeId, schedule.getScheduleId()))
                    .toList();

            log.info("일정 검색 완료: 키워드 '{}', 결과 {} 건", keyword, accessibleSchedules.size());
            return ResponseEntity.ok(accessibleSchedules);

        } catch (Exception e) {
            log.error("일정 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


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

    @GetMapping("/{scheduleId}/access")
    public ResponseEntity<Map<String, Serializable>> checkScheduleAccess(
            @PathVariable String scheduleId,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일정 접근 권한 확인 요청: 사용자 {}, 일정 ID {}", employeeId, scheduleId);

        try {
            boolean hasAccess = scheduleService.hasAccessToSchedule(employeeId, scheduleId);
            log.info("일정 접근 권한 확인 완료: {}", hasAccess);

            return ResponseEntity.ok(Map.of(
                    "hasAccess", hasAccess,
                    "scheduleId", scheduleId,
                    "employeeId", employeeId
            ));

        } catch (Exception e) {
            log.error("일정 접근 권한 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/{scheduleId}/participants")
    public ResponseEntity<List<String>> getParticipantList(
            @PathVariable String scheduleId,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("일정 참여자 목록 조회 요청: 사용자 {}, 일정 ID {}", employeeId, scheduleId);

        try {
            // 권한 체크
            if (!scheduleService.hasAccessToSchedule(employeeId, scheduleId)) {
                log.warn("일정 참여자 목록 접근 권한 없음: 사용자 {}, 일정 ID {}", employeeId, scheduleId);
                return ResponseEntity.status(403).build();
            }

            List<String> participants = scheduleService.getParticipantList(scheduleId);
            log.info("일정 참여자 목록 조회 완료: {} 명", participants.size());
            return ResponseEntity.ok(participants);

        } catch (Exception e) {
            log.error("일정 참여자 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/employee/{targetEmployeeId}")
    public ResponseEntity<List<Schedule>> getSchedulesByEmployee(
            @PathVariable String targetEmployeeId,
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("직원별 일정 조회 요청: 요청자 {}, 대상 직원 {}", employeeId, targetEmployeeId);

        try {
            // 🔧 본인 또는 관리자만 조회 가능 (추후 권한 체크 로직 확장)
            if (!employeeId.equals(targetEmployeeId)) {
                log.warn("다른 직원 일정 조회 권한 없음: 요청자 {}, 대상 {}", employeeId, targetEmployeeId);
                return ResponseEntity.status(403).build();
            }

            List<Schedule> schedules = scheduleService.getSchedulesByEmployee(targetEmployeeId);
            log.info("직원별 일정 조회 완료: {} 건", schedules.size());
            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("직원별 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/ongoing")
    public ResponseEntity<List<ScheduleResponseDTO>> getOngoingSchedules(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("진행 중인 일정 조회 요청: 사용자 {}", employeeId);

        try {
            List<ScheduleResponseDTO> allSchedules = scheduleService.getAccessibleSchedules(employeeId);

            List<ScheduleResponseDTO> ongoingSchedules = allSchedules.stream()
                    .filter(ScheduleResponseDTO::isOngoing)
                    .toList();

            log.info("진행 중인 일정 조회 완료: {} 건", ongoingSchedules.size());
            return ResponseEntity.ok(ongoingSchedules);

        } catch (Exception e) {
            log.error("진행 중인 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/this-week")
    public ResponseEntity<List<ScheduleResponseDTO>> getThisWeekSchedules(
            @RequestHeader(value = "X-Employee-Id", required = false) String employeeId) {

        if (employeeId == null) {
            employeeId = "TEMP_USER_001";
        }

        log.info("이번 주 일정 조회 요청: 사용자 {}", employeeId);

        try {
            List<ScheduleResponseDTO> allSchedules = scheduleService.getAccessibleSchedules(employeeId);

            List<ScheduleResponseDTO> thisWeekSchedules = allSchedules.stream()
                    .filter(ScheduleResponseDTO::isThisWeek)
                    .sorted((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                    .toList();

            log.info("이번 주 일정 조회 완료: {} 건", thisWeekSchedules.size());
            return ResponseEntity.ok(thisWeekSchedules);

        } catch (Exception e) {
            log.error("이번 주 일정 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "service", "ScheduleController",
                    "timestamp", LocalDateTime.now(),
                    "message", "일정 관리 서비스가 정상 작동 중입니다."
            ));
        } catch (Exception e) {
            log.error("건강 체크 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "DOWN",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }


    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        return ResponseEntity.ok(Map.of(
                "controller", "ScheduleController",
                "version", "1.0",
                "description", "일정 관리 API - SDD 명세 기반 구현",
                "features", List.of(
                        "일정 등록 (registerSchedule)",
                        "일정 조회 (displayAllSchedule, displaySchedule)",
                        "직원 검색 (searchEmployeeInfoByKeyword)",
                        "월별/일별 일정 조회",
                        "통계 정보",
                        "권한 체크",
                        "참여자 관리"
                ),
                "excludedFeatures", List.of("일정 수정", "일정 삭제"),
                "basedOn", "SDD 명세서 DCD3005"
        ));
    }
}
package com.example.gagso.Schedules.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.AlarmDomainType;
import com.example.gagso.Alarm.service.AlarmService;
import com.example.gagso.Employee.repository.EmployeeRepository;
import com.example.gagso.Employee.models.Employee;
import com.example.gagso.Schedules.dto.ScheduleRegisterRequestDTO;
import com.example.gagso.Schedules.dto.ScheduleRegistrationResult;
import com.example.gagso.Schedules.dto.ScheduleResponseDTO;
import com.example.gagso.Schedules.helper.ScheduleValidator;
import com.example.gagso.Schedules.models.Participant;
import com.example.gagso.Schedules.models.Schedule;
import com.example.gagso.Schedules.repository.ParticipantRepository;
import com.example.gagso.Schedules.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 일정 등록, 조회의 비즈니스 로직을 처리한다.
 * 저장소, 알림 예약, 유효성 검사, 로그 기록 등을 통합 조율한다.
 * 설계 명세: DCD3010 (알림 기능 통합 완료)
 *
 * 🔧 메소드 추적 결과:
 * - register() → convertToSchedule() → saveParticipants() → scheduleAlarmForSchedule() → writeLog()
 * - getAccessibleSchedules() → convertToScheduleResponseDTO() → getEmployeeName()
 * - getScheduleWithParticipants() → convertToScheduleResponseDTO()
 * - 에러 발생 지점: 참여자 조회, 알림 설정, DTO 변환 과정
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ParticipantRepository participantRepository;
    private final ScheduleValidator validator;
    private final EmployeeRepository employeeRepository;
    private final AlarmService alarmService;

    /**
     * Repository 접근을 위한 Getter (ScheduleController에서 사용)
     */
    public ScheduleRepository getScheduleRepository() {
        return scheduleRepository;
    }

    /**
     * 일정 등록, 유효성 검사, 로그 기록, 저장 요청, 알림 예약 요청,
     * 일정 등록 결과를 반환하는 일정 등록 전체 흐름
     * 설계 명세: register (알림 기능 완전 구현)
     *
     * 🔧 메소드 추적: register() → validator.validate() → convertToSchedule() →
     *                scheduleRepository.save() → saveParticipants() → scheduleAlarmForSchedule() → writeLog()
     */
    @Transactional
    public ScheduleRegistrationResult register(ScheduleRegisterRequestDTO scheduleDTO, String employeeId) {
        try {
            // 1. 입력값 검증 (Null 체크 추가)
            if (scheduleDTO == null) {
                log.error("일정 등록 요청 DTO가 null입니다");
                return ScheduleRegistrationResult.failure("validation", "일정 정보가 제공되지 않았습니다.");
            }

            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.error("직원 ID가 null 또는 빈 값입니다");
                return ScheduleRegistrationResult.failure("validation", "직원 ID가 필요합니다.");
            }

            // 2. 요청 DTO에 작성자 정보 설정
            scheduleDTO.setEmployeeId(employeeId);
            log.debug("일정 등록 요청: 직원 ID {}, 참여자 수 {}",
                    employeeId, scheduleDTO.getParticipantIds() != null ? scheduleDTO.getParticipantIds().size() : 0);

            // 3. 유효성 검사
            ScheduleRegistrationResult validationResult = validator.validate(scheduleDTO);
            if (!validationResult.isSuccess()) {
                log.warn("일정 등록 유효성 검사 실패: {}", validationResult.getErrors());
                return validationResult;
            }

            // 4. DTO를 엔티티로 변환
            Schedule schedule = convertToSchedule(scheduleDTO);
            if (schedule == null) {
                log.error("일정 엔티티 변환 실패");
                return ScheduleRegistrationResult.failure("conversion", "일정 정보 변환 중 오류가 발생했습니다.");
            }

            // 5. 일정 저장
            Schedule savedSchedule = scheduleRepository.save(schedule);
            log.info("일정 저장 완료: {}", savedSchedule.getScheduleId());

            // 6. 참여자 저장 (에러 처리 강화)
            try {
                saveParticipants(savedSchedule.getScheduleId(), scheduleDTO.getParticipantIds());
            } catch (Exception e) {
                log.error("참여자 저장 중 오류 발생", e);
                // 일정은 저장되었으므로 참여자 저장 실패만 알림
                log.warn("일정은 등록되었으나 참여자 저장에 실패했습니다: {}", savedSchedule.getScheduleId());
            }

            // 7. 알림 예약 (에러 처리 강화)
            if (scheduleDTO.hasAlarm()) {
                try {
                    scheduleAlarmForSchedule(savedSchedule, scheduleDTO);
                } catch (Exception e) {
                    log.error("알림 설정 중 오류 발생", e);
                    // 알림 실패는 치명적이지 않음
                }
            }

            // 8. 로그 기록 (에러 처리 강화)
            try {
                writeLog(employeeId, savedSchedule);
            } catch (Exception e) {
                log.error("로그 기록 중 오류 발생", e);
                // 로그 실패는 치명적이지 않음
            }

            return ScheduleRegistrationResult.success(savedSchedule);

        } catch (Exception e) {
            log.error("일정 등록 중 예상치 못한 오류 발생", e);
            return ScheduleRegistrationResult.failure("system", "일정 등록 중 시스템 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 🔧 수정: 해당 직원이 접근 가능한 일정 목록 반환 (참여자 정보 포함)
     * 설계 명세: getAccessibleSchedules
     *
     * 메소드 추적: getAccessibleSchedules() → scheduleRepository.findAccessibleSchedulesByEmployeeId()
     *             → convertToScheduleResponseDTO() → getEmployeeName()
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> getAccessibleSchedules(String employeeId) {
        try {
            // 입력값 검증
            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.warn("직원 ID가 null 또는 빈 값입니다");
                return List.of();
            }

            List<Schedule> schedules = scheduleRepository.findAccessibleSchedulesByEmployeeId(employeeId);
            log.info("직원 {}의 접근 가능한 일정 조회 완료: {} 건", employeeId, schedules.size());

            // 🔧 각 일정에 참여자 정보 포함해서 DTO로 변환 (에러 처리 강화)
            return schedules.stream()
                    .map(schedule -> {
                        try {
                            return convertToScheduleResponseDTO(schedule);
                        } catch (Exception e) {
                            log.error("일정 DTO 변환 중 오류 발생: scheduleId={}", schedule.getScheduleId(), e);
                            return createBasicScheduleDTO(schedule);
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("일정 목록 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 🔧 새로 추가: Schedule 엔티티를 ScheduleResponseDTO로 변환 (참여자 정보 포함)
     * 메소드 추적: convertToScheduleResponseDTO() → participantRepository.findParticipantListByScheduleId()
     *             → getEmployeeName()
     */
    private ScheduleResponseDTO convertToScheduleResponseDTO(Schedule schedule) {
        try {
            if (schedule == null) {
                throw new IllegalArgumentException("Schedule이 null입니다");
            }

            // 참여자 목록 조회 (에러 처리 강화)
            List<String> participantIds = List.of();
            List<String> participantNames = List.of();

            try {
                participantIds = participantRepository.findParticipantListByScheduleId(schedule.getScheduleId());
                log.debug("일정 {} 참여자 조회 완료: {} 명", schedule.getScheduleId(), participantIds.size());

                // 참여자 이름 조회 (employeeId -> name 변환)
                participantNames = participantIds.stream()
                        .map(this::getEmployeeName)
                        .collect(Collectors.toList());

            } catch (Exception e) {
                log.warn("참여자 정보 조회 실패: scheduleId={}", schedule.getScheduleId(), e);
                // 빈 리스트로 계속 진행
            }

            return ScheduleResponseDTO.builder()
                    .scheduleId(schedule.getScheduleId())
                    .title(schedule.getTitle())
                    .description(schedule.getDescription())
                    .startDate(schedule.getStartDate())
                    .endDate(schedule.getEndDate())
                    .visibility(schedule.getVisibility())
                    .alarmEnabled(schedule.getAlarmEnabled())
                    .alarmTime(schedule.getAlarmTime())
                    .employeeId(schedule.getEmployeeId())
                    .createdAt(schedule.getCreatedAt())
                    .updatedAt(schedule.getUpdatedAt())
                    .participants(participantNames) // 🔧 참여자 이름 리스트 추가
                    .participantIds(participantIds) // 🔧 참여자 ID 리스트 추가
                    .participantCount(participantIds.size()) // 🔧 참여자 수 추가
                    .createdBy(getEmployeeName(schedule.getEmployeeId())) // 🔧 작성자 이름 추가
                    .build();

        } catch (Exception e) {
            log.error("일정 DTO 변환 중 오류 발생: {}", schedule != null ? schedule.getScheduleId() : "null", e);
            // 기본 정보만 포함한 DTO 반환
            return createBasicScheduleDTO(schedule);
        }
    }

    /**
     * 🔧 새로 추가: 에러 발생 시 기본 일정 DTO 생성
     */
    private ScheduleResponseDTO createBasicScheduleDTO(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        return ScheduleResponseDTO.builder()
                .scheduleId(schedule.getScheduleId())
                .title(schedule.getTitle() != null ? schedule.getTitle() : "제목 없음")
                .description(schedule.getDescription() != null ? schedule.getDescription() : "")
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .visibility(schedule.getVisibility())
                .alarmEnabled(schedule.getAlarmEnabled() != null ? schedule.getAlarmEnabled() : false)
                .alarmTime(schedule.getAlarmTime())
                .employeeId(schedule.getEmployeeId())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .participants(List.of()) // 빈 리스트
                .participantIds(List.of()) // 빈 리스트
                .participantCount(0)
                .createdBy("알 수 없음")
                .build();
    }

    /**
     * 🔧 새로 추가: 직원 ID로 직원 이름 조회 (에러 처리 강화)
     */
    private String getEmployeeName(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return "알 수 없는 사용자";
        }

        try {
            Optional<Employee> employee = employeeRepository.findById(employeeId);
            return employee.map(Employee::getName).orElse("사용자(" + employeeId + ")");
        } catch (Exception e) {
            log.warn("직원 이름 조회 실패: employeeId={}", employeeId, e);
            return "사용자(" + employeeId + ")";
        }
    }

    /**
     * 특정 일정 조회 (참여자 정보 포함)
     * 메소드 추적: getScheduleWithParticipants() → scheduleRepository.findByScheduleId() → convertToScheduleResponseDTO()
     */
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getScheduleWithParticipants(String scheduleId) {
        try {
            if (scheduleId == null || scheduleId.trim().isEmpty()) {
                throw new IllegalArgumentException("일정 ID가 제공되지 않았습니다");
            }

            Schedule schedule = scheduleRepository.findByScheduleId(scheduleId)
                    .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));

            return convertToScheduleResponseDTO(schedule);

        } catch (Exception e) {
            log.error("일정 상세 조회 중 오류 발생: scheduleId={}", scheduleId, e);
            throw e; // Controller에서 처리하도록 재throw
        }
    }

    /**
     * 기존 getSchedule 메소드 (하위 호환성 유지)
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("일정 ID가 제공되지 않았습니다");
        }

        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));
    }

    /**
     * 특정 직원의 일정 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByEmployee(String employeeId) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.warn("직원 ID가 null 또는 빈 값입니다");
                return List.of();
            }

            return scheduleRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
        } catch (Exception e) {
            log.error("직원별 일정 조회 중 오류 발생: employeeId={}", employeeId, e);
            return List.of();
        }
    }

    /**
     * 🔧 수정: 월별 일정 조회 (성능 최적화용) - 참여자 정보 포함
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> getAccessibleSchedulesByMonth(String employeeId, int year, int month) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.warn("직원 ID가 null 또는 빈 값입니다");
                return List.of();
            }

            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            log.info("월별 일정 조회: 사용자 {}, 기간 {} ~ {}", employeeId, startOfMonth, endOfMonth);

            List<Schedule> monthlySchedules = scheduleRepository
                    .findAccessibleSchedulesByEmployeeIdAndDateRange(employeeId, startOfMonth, endOfMonth);

            log.info("월별 일정 조회 완료: {} 건", monthlySchedules.size());

            // 🔧 각 일정에 참여자 정보 포함해서 DTO로 변환
            return monthlySchedules.stream()
                    .map(schedule -> {
                        try {
                            return convertToScheduleResponseDTO(schedule);
                        } catch (Exception e) {
                            log.error("월별 일정 DTO 변환 중 오류 발생: scheduleId={}", schedule.getScheduleId(), e);
                            return createBasicScheduleDTO(schedule);
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("월별 일정 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 🔧 수정: 특정 날짜의 일정 조회 - 참여자 정보 포함
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> getAccessibleSchedulesByDate(String employeeId, LocalDate targetDate) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.warn("직원 ID가 null 또는 빈 값입니다");
                return List.of();
            }

            if (targetDate == null) {
                log.warn("대상 날짜가 null입니다");
                return List.of();
            }

            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

            log.info("일별 일정 조회: 사용자 {}, 날짜 {}", employeeId, targetDate);

            List<Schedule> dailySchedules = scheduleRepository
                    .findAccessibleSchedulesByEmployeeIdAndDateRange(employeeId, startOfDay, endOfDay);

            log.info("일별 일정 조회 완료: {} 건", dailySchedules.size());

            // 🔧 각 일정에 참여자 정보 포함해서 DTO로 변환
            return dailySchedules.stream()
                    .map(schedule -> {
                        try {
                            return convertToScheduleResponseDTO(schedule);
                        } catch (Exception e) {
                            log.error("일별 일정 DTO 변환 중 오류 발생: scheduleId={}", schedule.getScheduleId(), e);
                            return createBasicScheduleDTO(schedule);
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("일별 일정 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 참여자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getParticipantList(String scheduleId) {
        try {
            if (scheduleId == null || scheduleId.trim().isEmpty()) {
                log.warn("일정 ID가 null 또는 빈 값입니다");
                return List.of();
            }

            List<String> result = participantRepository.findParticipantListByScheduleId(scheduleId);
            log.debug("참여자 목록 조회 완료: 일정 ID {}, 참여자 {} 명", scheduleId, result.size());
            return result;

        } catch (Exception e) {
            log.error("참여자 목록 조회 중 오류 발생: scheduleId={}", scheduleId, e);
            return List.of();
        }
    }

    /**
     * 일정 검색
     */
    @Transactional(readOnly = true)
    public List<Schedule> searchSchedules(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("검색 키워드가 null 또는 빈 값입니다");
                return List.of();
            }

            return scheduleRepository.findByTitleContainingIgnoreCase(keyword.trim());

        } catch (Exception e) {
            log.error("일정 검색 중 오류 발생: keyword={}", keyword, e);
            return List.of();
        }
    }

    /**
     * 일정 삭제
     */
    @Transactional
    public void deleteSchedule(String scheduleId) {
        try {
            if (scheduleId == null || scheduleId.trim().isEmpty()) {
                throw new IllegalArgumentException("일정 ID가 제공되지 않았습니다");
            }

            // 일정 존재 여부 확인
            if (!scheduleRepository.existsByScheduleId(scheduleId)) {
                throw new IllegalArgumentException("삭제할 일정을 찾을 수 없습니다: " + scheduleId);
            }

            // 참여자 관계 먼저 삭제
            participantRepository.deleteByScheduleId(scheduleId);

            // 일정 삭제
            scheduleRepository.deleteByScheduleId(scheduleId);

            log.info("일정 삭제 완료: {}", scheduleId);

        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생: scheduleId={}", scheduleId, e);
            throw e; // Controller에서 처리하도록 재throw
        }
    }

    /**
     * 일정 접근 권한 확인
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToSchedule(String employeeId, String scheduleId) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty() ||
                    scheduleId == null || scheduleId.trim().isEmpty()) {
                log.warn("직원 ID 또는 일정 ID가 null 또는 빈 값입니다");
                return false;
            }

            return scheduleRepository.findAccessibleSchedulesByEmployeeId(employeeId)
                    .stream()
                    .anyMatch(schedule -> schedule.getScheduleId().equals(scheduleId));

        } catch (Exception e) {
            log.error("일정 접근 권한 확인 중 오류 발생: employeeId={}, scheduleId={}", employeeId, scheduleId, e);
            return false;
        }
    }

    /**
     * 🔧 개선된 일정 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public ScheduleStatistics getScheduleStatistics(String employeeId) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.warn("직원 ID가 null 또는 빈 값입니다");
                return new ScheduleStatistics(0, 0, 0);
            }

            List<Schedule> allSchedules = scheduleRepository.findAccessibleSchedulesByEmployeeId(employeeId);
            LocalDate today = LocalDate.now();

            int totalCount = allSchedules.size();
            int todayCount = (int) allSchedules.stream()
                    .filter(schedule -> {
                        LocalDate scheduleDate = schedule.getStartDate().toLocalDate();
                        return scheduleDate.equals(today);
                    })
                    .count();

            int upcomingCount = (int) allSchedules.stream()
                    .filter(schedule -> {
                        LocalDate scheduleDate = schedule.getStartDate().toLocalDate();
                        return scheduleDate.isAfter(today);
                    })
                    .count();

            return new ScheduleStatistics(totalCount, todayCount, upcomingCount);

        } catch (Exception e) {
            log.error("일정 통계 조회 중 오류 발생: employeeId={}", employeeId, e);
            return new ScheduleStatistics(0, 0, 0);
        }
    }

    /**
     * 일정 통계 내부 클래스
     */
    public static class ScheduleStatistics {
        private final int totalCount;
        private final int todayCount;
        private final int upcomingCount;

        public ScheduleStatistics(int totalCount, int todayCount, int upcomingCount) {
            this.totalCount = totalCount;
            this.todayCount = todayCount;
            this.upcomingCount = upcomingCount;
        }

        public int getTotalCount() { return totalCount; }
        public int getTodayCount() { return todayCount; }
        public int getUpcomingCount() { return upcomingCount; }
    }

    /**
     * 참여자 저장 (에러 처리 강화)
     * 메소드 추적: saveParticipants() → participantRepository.saveAll()
     */
    private void saveParticipants(String scheduleId, List<String> participantIds) {
        try {
            if (scheduleId == null || scheduleId.trim().isEmpty()) {
                log.warn("일정 ID가 null 또는 빈 값입니다");
                return;
            }

            if (participantIds != null && !participantIds.isEmpty()) {
                // 유효한 참여자 ID만 필터링
                List<String> validParticipantIds = participantIds.stream()
                        .filter(id -> id != null && !id.trim().isEmpty())
                        .distinct() // 중복 제거
                        .collect(Collectors.toList());

                if (validParticipantIds.isEmpty()) {
                    log.info("유효한 참여자가 없습니다: scheduleId={}", scheduleId);
                    return;
                }

                List<Participant> participants = validParticipantIds.stream()
                        .map(employeeId -> Participant.of(scheduleId, employeeId))
                        .collect(Collectors.toList());

                participantRepository.saveAll(participants);
                log.info("참여자 저장 완료: 일정 ID {}, 참여자 {} 명", scheduleId, participants.size());
            } else {
                log.info("참여자가 지정되지 않았습니다: scheduleId={}", scheduleId);
            }

        } catch (Exception e) {
            log.error("참여자 저장 중 오류 발생: scheduleId={}", scheduleId, e);
            throw new RuntimeException("참여자 저장 중 오류가 발생했습니다", e);
        }
    }

    /**
     * DTO를 Schedule 엔티티로 변환 (에러 처리 강화)
     */
    private Schedule convertToSchedule(ScheduleRegisterRequestDTO scheduleDTO) {
        try {
            if (scheduleDTO == null) {
                throw new IllegalArgumentException("ScheduleRegisterRequestDTO가 null입니다");
            }

            return Schedule.builder()
                    .scheduleId(UUID.randomUUID().toString())
                    .title(scheduleDTO.getTitle() != null ? scheduleDTO.getTitle().trim() : "제목 없음")
                    .description(scheduleDTO.getDescription() != null ? scheduleDTO.getDescription().trim() : "")
                    .startDate(scheduleDTO.getStartDate())
                    .endDate(scheduleDTO.getEndDate())
                    .visibility(scheduleDTO.getVisibility())
                    .alarmEnabled(scheduleDTO.getAlarmEnabled() != null ? scheduleDTO.getAlarmEnabled() : false)
                    .alarmTime(scheduleDTO.getAlarmTime())
                    .employeeId(scheduleDTO.getEmployeeId())
                    .build();

        } catch (Exception e) {
            log.error("Schedule 엔티티 변환 중 오류 발생", e);
            throw new RuntimeException("일정 정보 변환 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 일정 알림 스케줄링 (에러 처리 강화)
     */
    private void scheduleAlarmForSchedule(Schedule schedule, ScheduleRegisterRequestDTO scheduleDTO) {
        try {
            if (schedule == null || scheduleDTO == null) {
                log.warn("일정 또는 DTO가 null입니다");
                return;
            }

            if (scheduleDTO.getAlarmTime() == null) {
                log.warn("알림 시간이 설정되지 않았습니다: scheduleId={}", schedule.getScheduleId());
                return;
            }

            log.info("일정 알림 스케줄링 시작: 일정 ID {}", schedule.getScheduleId());

            // 작성자 알림 설정
            Employee creator = employeeRepository.findByEmployeeId(schedule.getEmployeeId()).orElse(null);
            if (creator != null && creator.getPhoneNum() != null) {
                AlarmInfo creatorAlarm = AlarmInfo.builder()
                        .recipientPhone(creator.getPhoneNum())
                        .targetId(schedule.getScheduleId())
                        .title(schedule.getTitle() + " 일정 알림")
                        .description("예정된 일정: " + schedule.getDescription())
                        .noticeTime(scheduleDTO.getAlarmTime())
                        .domainType(AlarmDomainType.SCHEDULE)
                        .build();

                alarmService.scheduleAlarm(creatorAlarm);
                log.info("작성자 알림 스케줄링 완료: {}", creator.getEmployeeId());
            } else {
                log.warn("작성자 정보 또는 전화번호를 찾을 수 없습니다: employeeId={}", schedule.getEmployeeId());
            }

            // 참여자들에게도 알림 (선택사항)
            if (scheduleDTO.getParticipantIds() != null && !scheduleDTO.getParticipantIds().isEmpty()) {
                for (String participantId : scheduleDTO.getParticipantIds()) {
                    try {
                        Employee participant = employeeRepository.findByEmployeeId(participantId).orElse(null);
                        if (participant != null && participant.getPhoneNum() != null) {
                            AlarmInfo participantAlarm = AlarmInfo.builder()
                                    .recipientPhone(participant.getPhoneNum())
                                    .targetId(schedule.getScheduleId())
                                    .title("[참여 일정] " + schedule.getTitle())
                                    .description("참여 예정 일정: " + schedule.getDescription())
                                    .noticeTime(scheduleDTO.getAlarmTime())
                                    .domainType(AlarmDomainType.SCHEDULE)
                                    .build();

                            alarmService.scheduleAlarm(participantAlarm);
                            log.debug("참여자 알림 스케줄링 완료: {}", participantId);
                        }
                    } catch (Exception e) {
                        log.warn("참여자 알림 설정 실패: participantId={}", participantId, e);
                        // 개별 참여자 알림 실패는 전체 프로세스를 중단하지 않음
                    }
                }
            }

        } catch (Exception e) {
            log.error("일정 알림 스케줄링 중 오류 발생", e);
            throw new RuntimeException("알림 설정 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 로그 기록 (에러 처리 강화)
     */
    private void writeLog(String employeeId, Schedule schedule) {
        try {
            if (employeeId == null || schedule == null) {
                log.warn("로그 기록을 위한 필수 정보가 null입니다");
                return;
            }

            // 실제 로그 기록 로직 구현
            log.info("일정 등록 로그 기록: 사용자 {}, 일정 ID {}, 제목 {}",
                    employeeId, schedule.getScheduleId(), schedule.getTitle());

            // TODO: 실제 LogWriter 구현체 호출
            // logWriter.save(employeeId, ActionType.CREATE, schedule);

        } catch (Exception e) {
            log.error("로그 기록 중 오류 발생", e);
            // 로그 기록 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }
}
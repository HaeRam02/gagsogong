package com.example.gagso.Schedules.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.AlarmDomainType;
import com.example.gagso.Alarm.service.AlarmService;
import com.example.gagso.Employees.repository.EmployeeRepository;
import com.example.gagso.Employees.models.Employee;
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
import com.example.gagso.Log.service.ScheduleLogWriter;
import com.example.gagso.Log.model.ActionType;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ParticipantRepository participantRepository;
    private final ScheduleValidator validator;
    private final EmployeeRepository employeeRepository;
    private final AlarmService alarmService;
    private final ScheduleLogWriter scheduleLogWriter;

    public ScheduleRepository getScheduleRepository() {
        return scheduleRepository;
    }


    @Transactional
    public ScheduleRegistrationResult register(ScheduleRegisterRequestDTO scheduleDTO, String employeeId) {
        try {

            if (scheduleDTO == null) {
                log.error("일정 등록 요청 DTO가 null입니다");
                return ScheduleRegistrationResult.failure("validation", "일정 정보가 제공되지 않았습니다.");
            }

            if (employeeId == null || employeeId.trim().isEmpty()) {
                log.error("직원 ID가 제공되지 않았습니다");
                return ScheduleRegistrationResult.failure("validation", "직원 정보가 필요합니다.");
            }

            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다: " + employeeId));

            scheduleDTO.setEmployeeId(employeeId);
            log.debug("일정 등록 요청: 직원 ID {}, 참여자 수 {}",
                    employeeId, scheduleDTO.getParticipantIds() != null ? scheduleDTO.getParticipantIds().size() : 0);

            // 3. 유효성 검사
            ScheduleRegistrationResult validationResult = validator.validate(scheduleDTO);
            if (!validationResult.isSuccess()) {
                log.warn("일정 등록 유효성 검사 실패: {}", validationResult.getErrors());
                return validationResult;
            }


            Schedule schedule = convertToSchedule(scheduleDTO);
            if (schedule == null) {
                log.error("일정 엔티티 변환 실패");
                return ScheduleRegistrationResult.failure("conversion", "일정 정보 변환 중 오류가 발생했습니다.");
            }

            Schedule savedSchedule = scheduleRepository.save(schedule);
            log.info("일정 저장 완료: {}", savedSchedule.getScheduleId());

            // 6. 참여자 저장 (에러 처리 강화)
            if (scheduleDTO.getParticipants() != null && !scheduleDTO.getParticipants().isEmpty()) {
                saveParticipants(savedSchedule, scheduleDTO.getParticipants());
            }

            try {
                scheduleAlarmForSchedule(savedSchedule, scheduleDTO, employee);
            } catch (Exception alarmException) {
                System.out.println("((((((((((((((((((((((((((((((((((");
                log.warn("알람 설정 실패 (일정 등록은 성공): 일정 ID {}", savedSchedule.getScheduleId(), alarmException);
            }

            // 8. 로그 기록 (에러 처리 강화)

            // 8. 로그 기록
            try {
                // scheduleLogWriter 필드가 주입되어야 합니다. (아래 참고)
                scheduleLogWriter.save(employeeId, ActionType.REGISTER, savedSchedule);
                log.info("일정 등록 로그 기록 완료: 일정 ID {}", savedSchedule.getScheduleId());
            } catch (Exception logException) {
                log.error("일정 등록 로그 기록 중 오류 발생: 일정 ID {}", savedSchedule.getScheduleId(), logException);
                // 로그 기록 실패는 일정 등록 실패로 이어지지 않으므로 예외를 다시 throw하지 않습니다.
            }

            return ScheduleRegistrationResult.success(savedSchedule);

        } catch (Exception e) {
            log.error("일정 등록 중 예상치 못한 오류 발생", e);
            return ScheduleRegistrationResult.failure("system", "일정 등록 중 시스템 오류가 발생했습니다: " + e.getMessage());
        }
    }

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


    private String getEmployeeName(String employeeId) {
        try {
            return employeeRepository.findByEmployeeId(employeeId)
                    .map(Employee::getName)
                    .orElse("알 수 없음");
        } catch (Exception e) {
            log.warn("직원 이름 조회 실패: ID {}", employeeId, e);
            return "알 수 없음";
        }
    }


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


    @Transactional(readOnly = true)
    public Schedule getSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("일정 ID가 제공되지 않았습니다");
        }

        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));
    }

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


    private void saveParticipants(Schedule schedule, List<String> participantIds) {
        try {
            List<Participant> participants = participantIds.stream()
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .map(participantId -> Participant.builder()
                            .participantId(UUID.randomUUID().toString())
                            .scheduleId(schedule.getScheduleId())
                            .employeeId(participantId)
                            .build())
                    .collect(Collectors.toList());

            if (!participants.isEmpty()) {
                participantRepository.saveAll(participants);
                log.info("참여자 저장 완료: 일정 ID {}, 참여자 {} 명", schedule.getScheduleId(), participants.size());
            }

        } catch (Exception e) {
            throw new RuntimeException("참여자 저장 중 오류가 발생했습니다", e);
        }
    }


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

    private void scheduleAlarmForSchedule(Schedule schedule, ScheduleRegisterRequestDTO scheduleDTO, Employee creator) {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~");

        if (schedule == null || scheduleDTO == null) {
            System.out.println("*******************************************");
            log.debug("일정 또는 DTO가 null이므로 알람 설정을 건너뜁니다");
            return;
        }

        // 알람이 활성화되지 않았으면 스킵
        if (!Boolean.TRUE.equals(scheduleDTO.getAlarmEnabled())) {
            System.out.println("%%%%%%%%%%%%%%%%%%%");

            log.debug("알람이 비활성화되어 있습니다: 일정 ID {}", schedule.getScheduleId());
            return;
        }

        // 알람 시간이 설정되지 않았으면 기본값 사용 (일정 시작 30분 전)
        LocalDateTime alarmTime = scheduleDTO.getAlarmTime();
        if (alarmTime == null) {
            System.out.println("$$$$$$$$$$$$$$$$$");

            alarmTime = schedule.getStartDate().minusMinutes(30);
            log.info("알람 시간이 설정되지 않아 기본값 사용: 일정 시작 30분 전 ({})", alarmTime);
        }

        // 과거 시간이면 알람 설정 안함
        if (alarmTime.isBefore(LocalDateTime.now())) {
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

            log.warn("알람 시간이 과거입니다. 알람을 설정하지 않습니다: {}", alarmTime);
            return;
        }

        log.info("일정 알람 설정 시작: 일정 ID {}, 알람 시간 {}", schedule.getScheduleId(), alarmTime);

        // 1. 작성자 알람 설정
        if (creator != null && creator.getPhoneNum() != null && !creator.getPhoneNum().trim().isEmpty()) {
            System.out.println("#############################");
            scheduleCreatorAlarm(schedule, alarmTime, creator);
        } else {
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@");

            log.warn("작성자 정보가 없거나 전화번호가 없어 작성자 알람을 설정할 수 없습니다");
        }

        // 2. 참여자 알람 설정
        if (scheduleDTO.getParticipants() != null && !scheduleDTO.getParticipants().isEmpty()) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");

            scheduleParticipantAlarms(schedule, alarmTime, scheduleDTO.getParticipants());
        }

        System.out.println("일정 알람 설정 완료: 일정 ID ================ "+ schedule.getAlarmEnabled());
    }

    private void scheduleCreatorAlarm(Schedule schedule, LocalDateTime alarmTime, Employee creator) {
        try {
            AlarmInfo creatorAlarm = AlarmInfo.forSchedule(
                    creator.getPhoneNum(),
                    schedule.getScheduleId(),
                    "[내 일정] " + schedule.getTitle(),
                    String.format("작성하신 일정이 곧 시작됩니다.\n위치: %s\n시간: %s",
                            schedule.getDescription() != null ? schedule.getDescription() : "미정",
                            schedule.getStartDate()),
                    alarmTime
            );

            String alarmId = alarmService.scheduleAlarm(creatorAlarm);
            log.info("작성자 알람 설정 완료: 알람 ID {}, 수신자 {}", alarmId, maskPhoneNumber(creator.getPhoneNum()));

        } catch (Exception e) {
            log.error("작성자 알람 설정 실패: 일정 ID {}, 작성자 ID {}", schedule.getScheduleId(), creator.getEmployeeId(), e);
        }
    }

    private String getCreatorName(String employeeId) {
        return getEmployeeName(employeeId);
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "****";
        }

        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanPhone.length() == 11) {
            return cleanPhone.substring(0, 3) + "-****-" + cleanPhone.substring(7);
        } else if (cleanPhone.length() == 10) {
            return cleanPhone.substring(0, 3) + "-***-" + cleanPhone.substring(6);
        } else {
            return "****";
        }
    }

    private void scheduleParticipantAlarms(Schedule schedule, LocalDateTime alarmTime, List<String> participantIds) {
        log.info("참여자 알람 설정 시작: {} 명", participantIds.size());

        int successCount = 0;
        int failureCount = 0;

        for (String participantId : participantIds) {
            try {
                Employee participant = employeeRepository.findByEmployeeId(participantId).orElse(null);

                if (participant == null) {
                    log.warn("참여자를 찾을 수 없습니다: ID {}", participantId);
                    failureCount++;
                    continue;
                }

                if (participant.getPhoneNum() == null || participant.getPhoneNum().trim().isEmpty()) {
                    log.warn("참여자의 전화번호가 없습니다: ID {}, 이름 {}", participantId, participant.getName());
                    failureCount++;
                    continue;
                }

                AlarmInfo participantAlarm = AlarmInfo.forSchedule(
                        participant.getPhoneNum(),
                        schedule.getScheduleId() + "_PARTICIPANT_" + participantId,
                        "[참여 일정] " + schedule.getTitle(),
                        String.format("참여하실 일정이 곧 시작됩니다.\n주최자: %s\n위치: %s\n시간: %s",
                                getCreatorName(schedule.getEmployeeId()),
                                schedule.getDescription() != null ? schedule.getDescription() : "미정",
                                schedule.getStartDate()),
                        alarmTime
                );

                String alarmId = alarmService.scheduleAlarm(participantAlarm);
                log.debug("참여자 알람 설정 완료: 알람 ID {}, 참여자 {}", alarmId, participant.getName());
                successCount++;

            } catch (Exception e) {
                log.error("참여자 알람 설정 실패: 일정 ID {}, 참여자 ID {}", schedule.getScheduleId(), participantId, e);
                failureCount++;
            }
        }

        log.info("참여자 알람 설정 완료: 성공 {} 명, 실패 {} 명", successCount, failureCount);
    }




    public void deleteScheduleWithAlarms(String scheduleId) {
        try {
            // 1. 관련 알람 모두 취소
            alarmService.cancelAlarmsByTarget(scheduleId, AlarmDomainType.SCHEDULE);
            log.info("일정 관련 알람 취소 완료: 일정 ID {}", scheduleId);

            // 2. 참여자 알람도 취소 (참여자별 알람이 있는 경우)
            List<Participant> participants = participantRepository.findByScheduleId(scheduleId);
            for (Participant participant : participants) {
                String participantAlarmId = scheduleId + "_PARTICIPANT_" + participant.getEmployeeId();
                try {
                    alarmService.cancelAlarm(participantAlarmId);
                } catch (Exception e) {
                    log.debug("참여자 알람 취소 중 오류 (무시): {}", participantAlarmId, e);
                }
            }

            // 3. 일정 삭제
            scheduleRepository.deleteById(scheduleId);
            log.info("일정 삭제 완료: ID {}", scheduleId);

        } catch (Exception e) {
            log.error("일정 삭제 실패: ID {}", scheduleId, e);
            throw new RuntimeException("일정 삭제 중 오류가 발생했습니다", e);
        }
    }

    @Transactional
    public void updateScheduleTimeWithAlarms(String scheduleId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        try {
            // 1. 기존 알람 취소
            alarmService.cancelAlarmsByTarget(scheduleId, AlarmDomainType.SCHEDULE);

            // 2. 일정 시간 업데이트
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));

            schedule.setStartDate(newStartTime);
            schedule.setEndDate(newEndTime);
            schedule.setUpdatedAt(LocalDateTime.now());

            Schedule updatedSchedule = scheduleRepository.save(schedule);

            // 3. 새로운 알람 설정
            if (schedule.getAlarmEnabled()) {
                Employee creator = employeeRepository.findByEmployeeId(schedule.getEmployeeId()).orElse(null);
                if (creator != null) {
                    LocalDateTime newAlarmTime = newStartTime.minusMinutes(30); // 30분 전 알람
                    scheduleCreatorAlarm(schedule, newAlarmTime, creator);
                    log.info("일정 시간 변경에 따른 알람 재설정 완료: 일정 ID {}", scheduleId);
                }
            }

        } catch (Exception e) {
            log.error("일정 시간 수정 실패: ID {}", scheduleId, e);
            throw new RuntimeException("일정 시간 수정 중 오류가 발생했습니다", e);
        }
    }


    @Transactional(readOnly = true)
    public boolean hasActiveAlarms(String scheduleId) {
        try {
            List<com.example.gagso.Alarm.models.Alarm> alarms =
                    alarmService.getAlarmsByTarget(scheduleId, AlarmDomainType.SCHEDULE);
            return !alarms.isEmpty();
        } catch (Exception e) {
            log.warn("알람 상태 조회 실패: 일정 ID {}", scheduleId, e);
            return false;
        }
    }
}
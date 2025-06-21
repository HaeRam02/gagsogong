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
     */
    @Transactional
    public ScheduleRegistrationResult register(ScheduleRegisterRequestDTO scheduleDTO, String employeeId) {
        // 1. 요청 DTO에 작성자 정보 설정
        scheduleDTO.setEmployeeId(employeeId);
        System.out.println("##################################" + scheduleDTO.getParticipantIds());

        // 2. 유효성 검사
        ScheduleRegistrationResult validationResult = validator.validate(scheduleDTO);
        if (!validationResult.isSuccess()) {
            log.warn("일정 등록 유효성 검사 실패: {}", validationResult.getErrors());
            return validationResult;
        }

        try {
            // 3. DTO를 엔티티로 변환
            Schedule schedule = convertToSchedule(scheduleDTO);

            // 4. 일정 저장
            Schedule savedSchedule = scheduleRepository.save(schedule);
            log.info("일정 저장 완료: {}", savedSchedule.getScheduleId());

            // 5. 참여자 저장
            saveParticipants(savedSchedule.getScheduleId(), scheduleDTO.getParticipantIds());

            // 6. 알림 예약
            if (scheduleDTO.hasAlarm()) {
                scheduleAlarmForSchedule(savedSchedule, scheduleDTO);
            }

            // 7. 로그 기록
            writeLog(employeeId, savedSchedule);

            return ScheduleRegistrationResult.success(savedSchedule);

        } catch (Exception e) {
            log.error("일정 등록 중 오류 발생", e);
            return ScheduleRegistrationResult.failure("system", "일정 등록 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 🔧 수정: 해당 직원이 접근 가능한 일정 목록 반환 (참여자 정보 포함)
     * 설계 명세: getAccessibleSchedules
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> getAccessibleSchedules(String employeeId) {
        try {
            List<Schedule> schedules = scheduleRepository.findAccessibleSchedulesByEmployeeId(employeeId);
            log.info("직원 {}의 접근 가능한 일정 조회 완료: {} 건", employeeId, schedules.size());

            List<ScheduleResponseDTO> result = schedules.stream()
                    .map(this::convertToScheduleResponseDTO)
                    .toList();

            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+result.getFirst().getParticipantCount());

            // 🔧 각 일정에 참여자 정보 포함해서 DTO로 변환
            return schedules.stream()
                    .map(this::convertToScheduleResponseDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("일정 목록 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 🔧 새로 추가: Schedule 엔티티를 ScheduleResponseDTO로 변환 (참여자 정보 포함)
     */
    private ScheduleResponseDTO convertToScheduleResponseDTO(Schedule schedule) {
        try {
            // 참여자 목록 조회
            List<String> participantIds = participantRepository.findParticipantListByScheduleId(schedule.getScheduleId());
            System.out.println("pppppppppppppppppppppppppppppppppp"+participantIds);

            // 참여자 이름 조회 (employeeId -> name 변환)
            List<String> participantNames = participantIds.stream()
                    .map(this::getEmployeeName)
                    .collect(Collectors.toList());

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
                    .createdBy(getEmployeeName(schedule.getEmployeeId())) // 🔧 작성자 이름 추가
                    .build();

        } catch (Exception e) {
            log.error("일정 DTO 변환 중 오류 발생: {}", schedule.getScheduleId(), e);

            // 오류 발생 시 기본 정보만 포함한 DTO 반환
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
                    .participants(List.of()) // 빈 리스트
                    .participantIds(List.of()) // 빈 리스트
                    .createdBy("알 수 없음")
                    .build();
        }
    }

    /**
     * 🔧 새로 추가: 직원 ID로 직원 이름 조회
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
     */
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getScheduleWithParticipants(String scheduleId) {
        Schedule schedule = scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));
        return convertToScheduleResponseDTO(schedule);
    }

    /**
     * 기존 getSchedule 메소드 (하위 호환성 유지)
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(String scheduleId) {
        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));
    }

    /**
     * 특정 직원의 일정 목록 조회
     */
    public List<Schedule> getSchedulesByEmployee(String employeeId) {
        return scheduleRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
    }
    /**
     * 🔧 수정: 월별 일정 조회 (성능 최적화용) - 참여자 정보 포함
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponseDTO> getAccessibleSchedulesByMonth(String employeeId, int year, int month) {
        try {
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            log.info("월별 일정 조회: 사용자 {}, 기간 {} ~ {}", employeeId, startOfMonth, endOfMonth);

            List<Schedule> monthlySchedules = scheduleRepository
                    .findAccessibleSchedulesByEmployeeIdAndDateRange(employeeId, startOfMonth, endOfMonth);

            log.info("월별 일정 조회 완료: {} 건", monthlySchedules.size());

            // 🔧 각 일정에 참여자 정보 포함해서 DTO로 변환
            return monthlySchedules.stream()
                    .map(this::convertToScheduleResponseDTO)
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
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

            log.info("일별 일정 조회: 사용자 {}, 날짜 {}", employeeId, targetDate);

            List<Schedule> dailySchedules = scheduleRepository
                    .findAccessibleSchedulesByEmployeeIdAndDateRange(employeeId, startOfDay, endOfDay);

            log.info("일별 일정 조회 완료: {} 건", dailySchedules.size());

            // 🔧 각 일정에 참여자 정보 포함해서 DTO로 변환
            return dailySchedules.stream()
                    .map(this::convertToScheduleResponseDTO)
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
        List<String> result = participantRepository.findParticipantListByScheduleId(scheduleId);
        System.out.println("===================================" + result.size());
        return participantRepository.findParticipantListByScheduleId(scheduleId);
    }

    /**
     * 일정 검색
     */
    @Transactional(readOnly = true)
    public List<Schedule> searchSchedules(String keyword) {
        return scheduleRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * 일정 삭제
     */
    @Transactional
    public void deleteSchedule(String scheduleId) {
        // 참여자 관계 먼저 삭제
        participantRepository.deleteByScheduleId(scheduleId);

        // 일정 삭제
        scheduleRepository.deleteByScheduleId(scheduleId);

        log.info("일정 삭제 완료: {}", scheduleId);
    }

    /**
     * 일정 접근 권한 확인
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToSchedule(String employeeId, String scheduleId) {
        return scheduleRepository.findAccessibleSchedulesByEmployeeId(employeeId)
                .stream()
                .anyMatch(schedule -> schedule.getScheduleId().equals(scheduleId));
    }

    /**
     * 참여자 저장
     */
    private void saveParticipants(String scheduleId, List<String> participantIds) {
        if (participantIds != null && !participantIds.isEmpty()) {
            List<Participant> participants = participantIds.stream()
                    .map(employeeId -> Participant.of(scheduleId, employeeId))
                    .collect(Collectors.toList());

            participantRepository.saveAll(participants);

            System.out.println("----------------------" + participantRepository.findParticipantListByScheduleId(scheduleId));

            log.info("참여자 저장 완료: 일정 ID {}, 참여자 {} 명", scheduleId, participants.size());
        }
    }

    /**
     * DTO를 Schedule 엔티티로 변환
     */
    private Schedule convertToSchedule(ScheduleRegisterRequestDTO scheduleDTO) {
        return Schedule.builder()
                .scheduleId(UUID.randomUUID().toString())
                .title(scheduleDTO.getTitle())
                .description(scheduleDTO.getDescription())
                .startDate(scheduleDTO.getStartDate())
                .endDate(scheduleDTO.getEndDate())
                .visibility(scheduleDTO.getVisibility())
                .alarmEnabled(scheduleDTO.getAlarmEnabled())
                .alarmTime(scheduleDTO.getAlarmTime())
                .employeeId(scheduleDTO.getEmployeeId())
                .build();
    }

    /**
     * 일정 알림 스케줄링
     */
    private void scheduleAlarmForSchedule(Schedule schedule, ScheduleRegisterRequestDTO scheduleDTO) {
        try {
            log.info("일정 알림 스케줄링 시작: 일정 ID {}", schedule.getScheduleId());

            Employee creator = employeeRepository.findByEmployeeId(schedule.getEmployeeId()).orElse(null);
            if (creator != null) {
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
            }

            // 참여자들에게도 알림 (선택사항)
            if (scheduleDTO.getParticipantIds() != null) {
                for (String participantId : scheduleDTO.getParticipantIds()) {
                    Employee participant = employeeRepository.findByEmployeeId(participantId).orElse(null);
                    if (participant != null) {
                        AlarmInfo participantAlarm = AlarmInfo.builder()
                                .recipientPhone(participant.getPhoneNum())
                                .targetId(schedule.getScheduleId())
                                .title(schedule.getTitle() + " 일정 참여 알림")
                                .description("참여 예정 일정: " + schedule.getDescription())
                                .noticeTime(scheduleDTO.getAlarmTime())
                                .domainType(AlarmDomainType.SCHEDULE)
                                .build();

                        alarmService.scheduleAlarm(participantAlarm);
                    }
                }
                log.info("참여자 알림 스케줄링 완료: {} 명", scheduleDTO.getParticipantIds().size());
            }

        } catch (Exception e) {
            log.error("일정 알림 스케줄링 중 오류 발생", e);
        }
    }

    /**
     * 로그 기록
     */
    private void writeLog(String employeeId, Schedule schedule) {
        // TODO: 실제 로그 시스템 연동
        log.info("일정 등록 로그: 사용자 {}, 일정 ID {}, 제목 '{}'",
                employeeId, schedule.getScheduleId(), schedule.getTitle());
    }

    /**
     * 일정 통계 정보 (내부 클래스)
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleStatistics {
        private long totalCount;
        private long todayCount;
        private long upcomingCount;
        private long pastCount;
    }

    /**
     * 일정 통계 조회
     */
    @Transactional(readOnly = true)
    public ScheduleStatistics getScheduleStatistics(String employeeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<Schedule> allSchedules = scheduleRepository.findAccessibleSchedulesByEmployeeId(employeeId);

        long totalCount = allSchedules.size();
        long todayCount = allSchedules.stream()
                .mapToLong(s -> (s.getStartDate().toLocalDate().equals(today) ||
                        s.getEndDate().toLocalDate().equals(today)) ? 1 : 0)
                .sum();
        long upcomingCount = allSchedules.stream()
                .mapToLong(s -> s.getStartDate().isAfter(now) ? 1 : 0)
                .sum();
        long pastCount = allSchedules.stream()
                .mapToLong(s -> s.getEndDate().isBefore(now) ? 1 : 0)
                .sum();

        return ScheduleStatistics.builder()
                .totalCount(totalCount)
                .todayCount(todayCount)
                .upcomingCount(upcomingCount)
                .pastCount(pastCount)
                .build();
    }
}
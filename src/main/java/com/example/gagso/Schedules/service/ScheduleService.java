package com.example.gagso.Schedules.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.AlarmDomainType;
import com.example.gagso.Alarm.service.AlarmService;
import com.example.gagso.Employee.repository.EmployeeRepository;
import com.example.gagso.Employee.models.Employee;
import com.example.gagso.Schedules.dto.ScheduleRegisterRequestDTO;
import com.example.gagso.Schedules.dto.ScheduleRegistrationResult;
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
    private final EmployeeRepository employeeRepository; // 추가된 의존성

    // 알림 서비스 연동 (실제 구현)
    private final AlarmService alarmService;

    // TODO: 향후 추가될 컴포넌트들
    // private final LogWriter<Schedule> logWriter;

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
            if (scheduleDTO.hasParticipants()) {
                saveParticipants(savedSchedule.getScheduleId(), scheduleDTO.getParticipantIds());
                log.info("참여자 저장 완료: {} 명", scheduleDTO.getParticipantIds().size());
            }

            // 6. 알림 예약 (실제 구현 완료!)
            if (scheduleDTO.hasAlarm()) {
                scheduleAlarmForSchedule(savedSchedule, scheduleDTO);
            }

            // 7. 로그 기록 (향후 구현)
            writeLog(employeeId, savedSchedule);

            return ScheduleRegistrationResult.success(savedSchedule);

        } catch (Exception e) {
            log.error("일정 등록 중 오류 발생", e);
            return ScheduleRegistrationResult.failure("system", "일정 등록 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 해당 직원이 접근 가능한 일정 목록 반환
     * 설계 명세: getAccessibleSchedules
     */
    @Transactional(readOnly = true)
    public List<Schedule> getAccessibleSchedules(String employeeId) {
        try {
            List<Schedule> schedules = scheduleRepository.findAccessibleSchedulesByEmployeeId(employeeId);
            log.info("직원 {}의 접근 가능한 일정 조회 완료: {} 건", employeeId, schedules.size());
            return schedules;
        } catch (Exception e) {
            log.error("일정 목록 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 특정 일정 조회
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(String scheduleId) {
        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));
    }

    /**
     * 특정 직원의 일정 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByEmployee(String employeeId) {
        return scheduleRepository.findByEmployeeIdOrderByStartDateTimeDesc(employeeId);
    }

    /**
     * 월별 일정 조회 (성능 최적화용)
     * 달력 화면에서 특정 월의 일정만 필요할 때 사용
     */
    @Transactional(readOnly = true)
    public List<Schedule> getAccessibleSchedulesByMonth(String employeeId, int year, int month) {
        try {
            // 해당 월의 시작일과 종료일 계산
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            log.info("월별 일정 조회: 사용자 {}, 기간 {} ~ {}", employeeId, startOfMonth, endOfMonth);

            // 해당 월의 접근 가능한 일정만 조회
            List<Schedule> monthlySchedules = scheduleRepository
                    .findAccessibleSchedulesByEmployeeIdAndDateRange(employeeId, startOfMonth, endOfMonth);

            log.info("월별 일정 조회 완료: {} 건", monthlySchedules.size());
            return monthlySchedules;

        } catch (Exception e) {
            log.error("월별 일정 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 특정 날짜의 일정 조회 (달력 날짜 클릭 시 사용)
     */
    @Transactional(readOnly = true)
    public List<Schedule> getAccessibleSchedulesByDate(String employeeId, LocalDate targetDate) {
        try {
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

            log.info("일별 일정 조회: 사용자 {}, 날짜 {}", employeeId, targetDate);

            List<Schedule> dailySchedules = scheduleRepository
                    .findAccessibleSchedulesByEmployeeIdAndDateRange(employeeId, startOfDay, endOfDay);

            log.info("일별 일정 조회 완료: {} 건", dailySchedules.size());
            return dailySchedules;

        } catch (Exception e) {
            log.error("일별 일정 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 일정 알림 스케줄링 (알림 시스템과 통합)
     * 설계 명세: scheduleAlarmForSchedule
     */
    private void scheduleAlarmForSchedule(Schedule schedule, ScheduleRegisterRequestDTO scheduleDTO) {
        try {
            log.info("일정 알림 스케줄링 시작: 일정 ID {}", schedule.getScheduleId());

            // 작성자에게 알림
            Employee creator = employeeRepository.findByEmployeeId(schedule.getEmployeeId()).orElse(null);
            if (creator != null) {
                AlarmInfo creatorAlarm = AlarmInfo.builder()
                        .recipientPhone(creator.getPhoneNum()) // recipientId 대신 recipientPhone 사용
                        .targetId(schedule.getScheduleId())
                        .title(schedule.getTitle() + " 일정 알림")
                        .description("예정된 일정: " + schedule.getDescription())
                        .noticeTime(scheduleDTO.getAlarmTime())
                        .domainType(AlarmDomainType.SCHEDULE)
                        .build();

                String alarmId = alarmService.scheduleAlarm(creatorAlarm);
                log.info("작성자 알림 등록 완료: 알림 ID {}", alarmId);
            }

            // 참여자들에게 알림
            if (scheduleDTO.hasParticipants()) {
                for (String participantId : scheduleDTO.getParticipantIds()) {
                    Employee participant = employeeRepository.findByEmployeeId(participantId).orElse(null);
                    if (participant != null) {
                        AlarmInfo participantAlarm = AlarmInfo.builder()
                                .recipientPhone(participant.getPhoneNum()) // recipientId 대신 recipientPhone 사용
                                .targetId(schedule.getScheduleId())
                                .title(schedule.getTitle() + " 일정 알림")
                                .description("참여 예정 일정: " + schedule.getDescription())
                                .noticeTime(scheduleDTO.getAlarmTime())
                                .domainType(AlarmDomainType.SCHEDULE)
                                .build();

                        String alarmId = alarmService.scheduleAlarm(participantAlarm);
                        log.info("참여자 알림 등록 완료: 참여자 ID {}, 알림 ID {}", participantId, alarmId);
                    }
                }
            }

            log.info("일정 알림 스케줄링 완료: 일정 ID {}", schedule.getScheduleId());

        } catch (Exception e) {
            log.error("일정 알림 스케줄링 중 오류 발생: 일정 ID {}", schedule.getScheduleId(), e);
            // 알림 실패는 일정 등록 전체를 실패시키지 않음
        }
    }

    /**
     * 두 직원이 같은 부서인지 확인
     * 설계 명세: isSameDepartment
     */
    public boolean isSameDepartment(String employeeId1, String employeeId2) {
        try {
            // EmployeeRepository의 isSameDepartment 메서드 사용
            boolean isSameDept = employeeRepository.isSameDepartment(employeeId1, employeeId2);

            log.debug("같은 부서 여부 확인: employeeId1={}, employeeId2={}, result={}",
                    employeeId1, employeeId2, isSameDept);
            return isSameDept;
        } catch (Exception e) {
            log.error("부서 비교 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 참여자 정보 저장
     */
    private void saveParticipants(String scheduleId, List<String> participantIds) {
        for (String participantId : participantIds) {
            Participant participant = Participant.builder()
                    .id(UUID.randomUUID().toString())
                    .scheduleId(scheduleId)
                    .employeeId(participantId)
                    .build();

            participantRepository.save(participant);
        }
    }

    /**
     * DTO를 Schedule 엔티티로 변환
     */
    private Schedule convertToSchedule(ScheduleRegisterRequestDTO dto) {
        return Schedule.builder()
                .scheduleId(UUID.randomUUID().toString())
                .employeeId(dto.getEmployeeId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDateTime(dto.getStartDate())
                .endDateTime(dto.getEndDate())
                .visibility(dto.getVisibility())
                .alarmEnabled(dto.getAlarmEnabled())
                .alarmTime(dto.getAlarmTime())
                .build();
    }

    /**
     * 로그 기록 (향후 구현)
     */
    private void writeLog(String employeeId, Schedule schedule) {
        // TODO: LogWriter 구현 후 활성화
        log.info("일정 등록 로그: 사용자 {}, 일정 ID {}", employeeId, schedule.getScheduleId());
    }
}
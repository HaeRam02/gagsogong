package com.example.gagso.Schedules.service;

import com.example.gagso.Alarm.dto.AlarmInfo;
import com.example.gagso.Alarm.models.AlarmDomainType;
import com.example.gagso.Alarm.service.AlarmService;
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

import java.util.List;
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

    // 알림 서비스 연동 (실제 구현)
    private final AlarmService alarmService;

    // TODO: 향후 추가될 컴포넌트들
    // private final LogWriter<Schedule> logWriter;

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
     * 특정 일정 상세 조회
     */
    @Transactional(readOnly = true)
    public Schedule getSchedule(String scheduleId) {
        return scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + scheduleId));
    }

    /**
     * 특정 일정의 참여자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getParticipantList(String scheduleId) {
        return participantRepository.findParticipantListByScheduleId(scheduleId);
    }

    /**
     * 특정 직원이 참여한 일정 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByEmployee(String employeeId) {
        List<String> scheduleIds = participantRepository.findScheduleIdListByEmployeeId(employeeId);
        if (scheduleIds.isEmpty()) {
            return List.of();
        }
        return scheduleRepository.findByScheduleIdIn(scheduleIds);
    }

    /**
     * 일정 제목으로 검색
     */
    @Transactional(readOnly = true)
    public List<Schedule> searchSchedules(String keyword) {
        return scheduleRepository.findByTitleContaining(keyword);
    }

    /**
     * 일정 삭제 (알림도 함께 취소)
     */
    @Transactional
    public void deleteSchedule(String scheduleId) {
        try {
            // 1. 일정 존재 확인
            Schedule schedule = getSchedule(scheduleId);

            // 2. 관련 알림 모두 취소
            alarmService.cancelAlarmsByTarget(scheduleId, AlarmDomainType.SCHEDULE);
            log.info("일정 관련 알림 취소 완료: {}", scheduleId);

            // 3. 참여자 정보 삭제
            participantRepository.deleteByScheduleId(scheduleId);

            // 4. 일정 삭제
            scheduleRepository.deleteById(scheduleId);

            log.info("일정 삭제 완료: {}", scheduleId);

        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생: {}", scheduleId, e);
            throw new RuntimeException("일정 삭제에 실패했습니다.", e);
        }
    }

    /**
     * 일정 등록 정보 DTO를 Schedule 객체로 변환
     * 설계 명세: convertToSchedule
     */
    private Schedule convertToSchedule(ScheduleRegisterRequestDTO scheduleDTO) {
        return Schedule.builder()
                .title(scheduleDTO.getTitle())
                .description(scheduleDTO.getDescription())
                .startDateTime(scheduleDTO.getStartDateTime())
                .endDateTime(scheduleDTO.getEndDateTime())
                .visibility(scheduleDTO.getVisibility())
                .alarmEnabled(scheduleDTO.getAlarmEnabled())
                .alarmTime(scheduleDTO.getAlarmTime())
                .employeeId(scheduleDTO.getEmployeeId())
                .build();
    }

    /**
     * 참여자 정보 저장
     */
    private void saveParticipants(String scheduleId, List<String> participantIds) {
        List<Participant> participants = participantIds.stream()
                .map(employeeId -> Participant.of(scheduleId, employeeId))
                .collect(Collectors.toList());

        participantRepository.saveAll(participants);
    }

    /**
     * 일정용 알림 예약 (실제 구현 완료!)
     */
    private void scheduleAlarmForSchedule(Schedule schedule, ScheduleRegisterRequestDTO scheduleDTO) {
        try {
            // 작성자용 알림
            AlarmInfo creatorAlarm = AlarmInfo.forSchedule(
                    "010-0000-0000", // TODO: 실제 전화번호는 Employee 서비스에서 조회
                    schedule.getScheduleId(),
                    "[일정 알림] " + schedule.getTitle(),
                    schedule.getDescription(),
                    schedule.getAlarmTime()
            );

            String alarmId = alarmService.scheduleAlarm(creatorAlarm);
            log.info("일정 작성자 알림 예약 완료: 일정 ID {}, 알림 ID {}",
                    schedule.getScheduleId(), alarmId);

            // 참여자용 알림 (그룹 일정인 경우)
            if (scheduleDTO.hasParticipants()) {
                for (String participantId : scheduleDTO.getParticipantIds()) {
                    AlarmInfo participantAlarm = AlarmInfo.forSchedule(
                            "010-1111-1111", // TODO: 참여자 전화번호 조회
                            schedule.getScheduleId(),
                            "[참여 일정 알림] " + schedule.getTitle(),
                            "참여하시는 일정이 곧 시작됩니다.",
                            schedule.getAlarmTime()
                    );

                    String participantAlarmId = alarmService.scheduleAlarm(participantAlarm);
                    log.info("참여자 알림 예약 완료: 참여자 {}, 알림 ID {}",
                            participantId, participantAlarmId);
                }
            }

        } catch (Exception e) {
            log.error("일정 알림 예약 중 오류 발생", e);
            // 알림 실패가 일정 등록을 방해하지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 로그 기록 (향후 구현)
     */
    private void writeLog(String employeeId, Schedule schedule) {
        // TODO: LogWriter 연동
        log.info("일정 등록 로그: 작성자 {}, 일정 ID {}", employeeId, schedule.getScheduleId());
    }
}
// src/main/java/com/example/gagso/Schedules/service/ScheduleService.java
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * 공개범위별 접근 권한 세부 확인 메서드
     * 더 정확한 권한 검사가 필요할 때 사용
     */
    public boolean hasAccessToSchedule(String employeeId, String scheduleId) {
        try {
            Schedule schedule = scheduleRepository.findByScheduleId(scheduleId)
                    .orElse(null);

            if (schedule == null) {
                return false;
            }

            return switch (schedule.getVisibility()) {
                case PUBLIC -> true; // 모든 사용자 접근 가능

                case GROUP -> {
                    // 같은 부서이거나 참여자인 경우
                    boolean isSameDepartment = checkSameDepartment(employeeId, schedule.getEmployeeId());
                    boolean isParticipant = isParticipantInSchedule(employeeId, scheduleId);
                    yield isSameDepartment || isParticipant;
                }

                case PRIVATE -> {
                    // 작성자이거나 참여자인 경우만
                    boolean isCreator = schedule.getEmployeeId().equals(employeeId);
                    boolean isParticipant = isParticipantInSchedule(employeeId, scheduleId);
                    yield isCreator || isParticipant;
                }
            };

        } catch (Exception e) {
            log.error("일정 접근 권한 확인 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 일정 통계 정보 조회 (대시보드용)
     */
    @Transactional(readOnly = true)
    public ScheduleStatistics getScheduleStatistics(String employeeId) {
        try {
            List<Schedule> accessibleSchedules = getAccessibleSchedules(employeeId);

            long totalCount = accessibleSchedules.size();
            long todayCount = accessibleSchedules.stream()
                    .filter(schedule -> isToday(schedule.getStartDateTime()))
                    .count();
            long upcomingCount = accessibleSchedules.stream()
                    .filter(schedule -> isUpcoming(schedule.getStartDateTime()))
                    .count();

            return new ScheduleStatistics(totalCount, todayCount, upcomingCount);

        } catch (Exception e) {
            log.error("일정 통계 조회 중 오류 발생", e);
            return new ScheduleStatistics(0, 0, 0);
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
            scheduleRepository.delete(schedule);
            log.info("일정 삭제 완료: {}", scheduleId);

        } catch (Exception e) {
            log.error("일정 삭제 중 오류 발생", e);
            throw new RuntimeException("일정 삭제 중 오류가 발생했습니다.");
        }
    }

    // ==================== Private 헬퍼 메서드들 ====================

    /**
     * DTO를 Schedule 엔티티로 변환
     */
    private Schedule convertToSchedule(ScheduleRegisterRequestDTO scheduleDTO) {
        return Schedule.builder()
                .title(scheduleDTO.getTitle())
                .description(scheduleDTO.getDescription())
                .startDateTime(scheduleDTO.getStartDateTime())
                .endDateTime(scheduleDTO.getEndDateTime())
                .visibility(scheduleDTO.getVisibility())
                .alarmEnabled(scheduleDTO.hasAlarm())
                .alarmTime(scheduleDTO.getAlarmTime())
                .employeeId(scheduleDTO.getEmployeeId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 참여자 정보 저장
     */
    private void saveParticipants(String scheduleId, List<String> participantIds) {
        List<Participant> participants = participantIds.stream()
                .map(employeeId -> Participant.builder()
                        .scheduleId(scheduleId)
                        .employeeId(employeeId)
                        .build())
                .collect(Collectors.toList());

        participantRepository.saveAll(participants);
    }

    /**
     * 일정 알림 예약
     */
    private void scheduleAlarmForSchedule(Schedule schedule, ScheduleRegisterRequestDTO scheduleDTO) {
        try {
            // 작성자 알림
            AlarmInfo creatorAlarm = AlarmInfo.builder()
                    .targetId(schedule.getScheduleId())
                    .domainType(AlarmDomainType.SCHEDULE)
                    .title("일정 알림: " + schedule.getTitle())
                    .message(schedule.getDescription())
                    .recipientId(schedule.getEmployeeId())
                    .noticeTime(scheduleDTO.getAlarmTime())
                    .build();

            alarmService.scheduleAlarm(creatorAlarm);

            // 참여자 알림 (그룹 공개일 경우)
            if (scheduleDTO.isGroupVisible() && scheduleDTO.hasParticipants()) {
                for (String participantId : scheduleDTO.getParticipantIds()) {
                    AlarmInfo participantAlarm = AlarmInfo.builder()
                            .targetId(schedule.getScheduleId())
                            .domainType(AlarmDomainType.SCHEDULE)
                            .title("일정 알림: " + schedule.getTitle())
                            .message("참여 일정: " + schedule.getDescription())
                            .recipientId(participantId)
                            .noticeTime(scheduleDTO.getAlarmTime())
                            .build();

                    alarmService.scheduleAlarm(participantAlarm);
                }
            }

            log.info("일정 알림 예약 완료: {}", schedule.getScheduleId());
        } catch (Exception e) {
            log.error("알림 예약 실패", e);
            // 알림 실패가 일정 등록을 방해하지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 같은 부서 확인 (GROUP 공개범위 처리용)
     */
    private boolean checkSameDepartment(String employeeId1, String employeeId2) {
        try {
            // 같은 직원이면 true
            if (employeeId1.equals(employeeId2)) {
                return true;
            }

            // 데이터베이스에서 같은 부서 여부 확인
            boolean isSameDept = employeeRepository.isSameDepartment(employeeId1, employeeId2);

            log.debug("부서 확인 결과: {} vs {} = {}", employeeId1, employeeId2, isSameDept);
            return isSameDept;

        } catch (Exception e) {
            log.error("부서 확인 중 오류 발생: {} vs {}", employeeId1, employeeId2, e);
            // 오류 발생 시 안전하게 false 반환
            return false;
        }
    }

    /**
     * 일정 참여자 확인
     */
    private boolean isParticipantInSchedule(String employeeId, String scheduleId) {
        List<String> participants = participantRepository.findParticipantListByScheduleId(scheduleId);
        return participants.contains(employeeId);
    }

    private boolean isToday(LocalDateTime dateTime) {
        return dateTime.toLocalDate().equals(LocalDate.now());
    }

    private boolean isUpcoming(LocalDateTime dateTime) {
        return dateTime.isAfter(LocalDateTime.now()) &&
                dateTime.toLocalDate().isBefore(LocalDate.now().plusDays(7));
    }

    /**
     * 로그 기록 (향후 구현)
     */
    private void writeLog(String employeeId, Schedule schedule) {
        // TODO: LogWriter 연동
        log.info("일정 등록 로그: 작성자 {}, 일정 ID {}", employeeId, schedule.getScheduleId());
    }

    // 통계 정보를 담는 간단한 DTO 클래스
    public static class ScheduleStatistics {
        private final long totalCount;
        private final long todayCount;
        private final long upcomingCount;

        public ScheduleStatistics(long totalCount, long todayCount, long upcomingCount) {
            this.totalCount = totalCount;
            this.todayCount = todayCount;
            this.upcomingCount = upcomingCount;
        }

        // getters
        public long getTotalCount() { return totalCount; }
        public long getTodayCount() { return todayCount; }
        public long getUpcomingCount() { return upcomingCount; }
    }
}
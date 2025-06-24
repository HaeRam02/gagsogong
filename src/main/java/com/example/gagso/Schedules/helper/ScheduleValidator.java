package com.example.gagso.Schedules.helper;

import com.example.gagso.Schedules.dto.ScheduleRegisterRequestDTO;
import com.example.gagso.Schedules.dto.ScheduleRegistrationResult;
import com.example.gagso.Schedules.dto.ValidationResult;
import com.example.gagso.Schedules.models.Visibility;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;


@Component
public class ScheduleValidator {

    private static final int MAX_TITLE_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 200;


    public ScheduleRegistrationResult validate(ScheduleRegisterRequestDTO reqSchedule) {
        ValidationResult result = ValidationResult.ofSuccess();

        // 각 검증 메서드 호출
        checkTitle(reqSchedule, result);
        checkDescription(reqSchedule, result);
        checkStartEndTime(reqSchedule, result);
        checkVisibility(reqSchedule, result);
        checkParticipants(reqSchedule, result);
        checkAlarmSettings(reqSchedule, result);

        if (result.isSuccess()) {
            return ScheduleRegistrationResult.builder()
                    .result(true)
                    .validationResult(result)
                    .build();
        } else {
            return ScheduleRegistrationResult.failure(reqSchedule, result);
        }
    }


    private void checkTitle(ScheduleRegisterRequestDTO reqSchedule, ValidationResult result) {
        String title = reqSchedule.getTitle();

        if (!StringUtils.hasText(title)) {
            result.addError("title", "일정 제목은 필수 입력 항목입니다.");
            return;
        }

        if (title.trim().length() > MAX_TITLE_LENGTH) {
            result.addError("title", String.format("일정 제목은 %d자를 초과할 수 없습니다.", MAX_TITLE_LENGTH));
        }
    }


    private void checkDescription(ScheduleRegisterRequestDTO reqSchedule, ValidationResult result) {
        String description = reqSchedule.getDescription();

        if (StringUtils.hasText(description) && description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            result.addError("description", String.format("일정 설명은 %d자를 초과할 수 없습니다.", MAX_DESCRIPTION_LENGTH));
        }
    }


    private void checkStartEndTime(ScheduleRegisterRequestDTO reqSchedule, ValidationResult result) {
        // ✅ 수정: startDate, endDate getter 사용
        LocalDateTime startDateTime = reqSchedule.getStartDate();
        LocalDateTime endDateTime = reqSchedule.getEndDate();

        if (startDateTime == null) {
            result.addError("startDate", "시작 날짜와 시간을 입력해주세요.");
            return;
        }

        if (endDateTime == null) {
            result.addError("endDate", "종료 날짜와 시간을 입력해주세요.");
            return;
        }

        if (startDateTime.isAfter(endDateTime)) {
            result.addError("startDate", "시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        if (startDateTime.equals(endDateTime)) {
            result.addError("startDate", "시작 날짜와 종료 날짜는 같을 수 없습니다.");
        }

        // 과거 날짜 검증 (선택적)
        LocalDateTime now = LocalDateTime.now();
        if (endDateTime.isBefore(now)) {
            result.addError("endDate", "종료 날짜는 현재 시간 이후여야 합니다.");
        }

        // 시작 날짜도 현재 시간 이후인지 검증 추가
        if (startDateTime.isBefore(now)) {
            result.addError("startDate", "시작 날짜는 현재 시간 이후여야 합니다.");
        }
    }

    private void checkVisibility(ScheduleRegisterRequestDTO reqSchedule, ValidationResult result) {
        Visibility visibility = reqSchedule.getVisibility();

        if (visibility == null) {
            result.addError("visibility", "공개 범위는 필수 선택 항목입니다.");
        }
    }


    private void checkParticipants(ScheduleRegisterRequestDTO reqSchedule, ValidationResult result) {
        Visibility visibility = reqSchedule.getVisibility();

        if (Visibility.GROUP.equals(visibility)) {
            if (!reqSchedule.hasParticipants()) {
                result.addError("participantIds", "그룹 공개 일정은 참여자를 1명 이상 선택해야 합니다.");
            }
        }
    }


    private void checkAlarmSettings(ScheduleRegisterRequestDTO reqSchedule, ValidationResult result) {
        Boolean alarmEnabled = reqSchedule.getAlarmEnabled();
        LocalDateTime alarmTime = reqSchedule.getAlarmTime();

        if (Boolean.TRUE.equals(alarmEnabled)) {
            if (alarmTime == null) {
                result.addError("alarmTime", "알림이 활성화된 경우 알림 시간을 설정해야 합니다.");
                return;
            }

            // ✅ 수정: 올바른 getter 메소드 사용
            LocalDateTime startDateTime = reqSchedule.getStartDate();
            if (startDateTime != null && alarmTime.isAfter(startDateTime)) {
                result.addError("alarmTime", "알림 시간은 일정 시작 시간 이전이어야 합니다.");
            }

            // 알림 시간이 과거인지 검사
            LocalDateTime now = LocalDateTime.now();
            if (alarmTime.isBefore(now)) {
                result.addError("alarmTime", "알림 시간은 현재 시간 이후여야 합니다.");
            }
        }
    }
}
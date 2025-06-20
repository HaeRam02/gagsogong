package com.example.gagso.Schedules.dto;

import com.example.gagso.Schedules.models.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자가 입력한 일정 등록 정보를 담는 데이터 전달 객체
 * 설계 명세: DCD3004 - DTO ScheduleRegisterRequestDTO
 * 지속성: Transient
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRegisterRequestDTO {

    /**
     * 일정 작성자 ID (서버에서 설정)
     */
    private String employeeId;

    /**
     * 일정 제목
     */
    private String title;

    /**
     * 일정 설명
     */
    private String description;

    /**
     * 일정 시작 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    /**
     * 일정 종료 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    /**
     * 일정 공개 범위
     */
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    /**
     * 알람 설정 유무
     * @Builder.Default를 사용하여 기본값 설정
     */
    @Builder.Default
    private Boolean alarmEnabled = false;

    /**
     * 알람 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    /**
     * 참여자 ID 목록
     */
    private List<String> participantIds;

    /**
     * 비즈니스 메서드: 참여자가 있는지 확인
     */
    public boolean hasParticipants() {
        return participantIds != null && !participantIds.isEmpty();
    }

    /**
     * 비즈니스 메서드: 알람이 설정되어 있는지 확인
     */
    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    /**
     * 비즈니스 메서드: 유효한 일정 시간인지 확인
     */
    public boolean isValidScheduleTime() {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }

    /**
     * 비즈니스 메서드: 미래 일정인지 확인
     */
    public boolean isFutureSchedule() {
        return startDate != null && startDate.isAfter(LocalDateTime.now());
    }

    /**
     * 비즈니스 메서드: 일정 기간 (분 단위)
     */
    public long getDurationMinutes() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.Duration.between(startDate, endDate).toMinutes();
    }

    /**
     * 비즈니스 메서드: 알람 시간이 유효한지 확인
     */
    public boolean isValidAlarmTime() {
        if (!hasAlarm()) {
            return true; // 알람이 없으면 유효
        }
        return alarmTime != null &&
                alarmTime.isAfter(LocalDateTime.now()) &&
                alarmTime.isBefore(startDate);
    }

    /**
     * 유효성 검사를 위한 헬퍼 메서드
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                isValidScheduleTime() &&
                isFutureSchedule() &&
                isValidAlarmTime();
    }

    /**
     * 디버깅용 toString (중요 정보만)
     */
    @Override
    public String toString() {
        return String.format("ScheduleRegisterRequestDTO{title='%s', startDate=%s, endDate=%s, alarmEnabled=%s}",
                title, startDate, endDate, alarmEnabled);
    }
}
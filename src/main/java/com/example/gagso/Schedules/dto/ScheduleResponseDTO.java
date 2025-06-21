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
 * 일정 조회 시 클라이언트로 반환하는 응답 DTO
 * Schedule 엔티티 정보 + 참여자 정보를 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {

    /**
     * 일정 고유 ID
     */
    private String scheduleId;

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
    private Visibility visibility;

    /**
     * 알람 설정 유무
     */
    @Builder.Default
    private Boolean alarmEnabled = false;

    /**
     * 알람 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    /**
     * 일정 작성자 ID
     */
    private String employeeId;

    /**
     * 일정 작성자 이름
     */
    private String createdBy;

    /**
     * 참여자 이름 목록 (프론트엔드에서 join으로 표시)
     */
    @Builder.Default
    private List<String> participants = List.of();

    /**
     * 참여자 ID 목록 (내부 처리용)
     */
    @Builder.Default
    private List<String> participantIds = List.of();

    /**
     * 일정 생성 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 일정 수정 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 비즈니스 메서드: 참여자가 있는지 확인
     */
    public boolean hasParticipants() {
        return participants != null && !participants.isEmpty();
    }

    /**
     * 비즈니스 메서드: 특정 사용자가 참여자인지 확인
     */
    public boolean isParticipant(String employeeId) {
        return participantIds != null && participantIds.contains(employeeId);
    }

    /**
     * 비즈니스 메서드: 일정 작성자인지 확인
     */
    public boolean isCreator(String employeeId) {
        return this.employeeId != null && this.employeeId.equals(employeeId);
    }

    /**
     * 비즈니스 메서드: 특정 사용자가 접근 가능한지 확인
     */
    public boolean isAccessibleBy(String employeeId) {
        // 작성자이거나 참여자이거나 공개 일정인 경우
        return isCreator(employeeId) ||
                isParticipant(employeeId) ||
                Visibility.PUBLIC.equals(visibility);
    }

    /**
     * 비즈니스 메서드: 알람이 설정되어 있는지 확인
     */
    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    /**
     * 비즈니스 메서드: 현재 진행 중인 일정인지 확인
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && endDate != null &&
                now.isAfter(startDate) && now.isBefore(endDate);
    }

    /**
     * 비즈니스 메서드: 미래 일정인지 확인
     */
    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && startDate.isAfter(now);
    }

    /**
     * 비즈니스 메서드: 과거 일정인지 확인
     */
    public boolean isPast() {
        LocalDateTime now = LocalDateTime.now();
        return endDate != null && endDate.isBefore(now);
    }

    /**
     * 비즈니스 메서드: 오늘 일정인지 확인
     */
    public boolean isToday() {
        LocalDateTime now = LocalDateTime.now();
        return (startDate != null && startDate.toLocalDate().equals(now.toLocalDate())) ||
                (endDate != null && endDate.toLocalDate().equals(now.toLocalDate()));
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
     * 비즈니스 메서드: 참여자 수
     */
    public int getParticipantCount() {
        return participants != null ? participants.size() : 0;
    }

    /**
     * 디버깅용 toString
     */
    @Override
    public String toString() {
        return String.format("ScheduleResponseDTO{scheduleId='%s', title='%s', startDate=%s, participantCount=%d}",
                scheduleId, title, startDate, getParticipantCount());
    }
}
package com.example.gagso.Schedules.dto;

import com.example.gagso.Schedules.models.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {


    private String scheduleId;

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    private Visibility visibility;

    @Builder.Default
    private Boolean alarmEnabled = false;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    private String employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;


    private String createdBy;

    @Builder.Default
    private List<String> participants = List.of();

    @Builder.Default
    private List<String> participantIds = List.of();

    @Builder.Default
    private Integer participantCount = 0;


    private String status;

    private Long minutesUntilStart;

    private Integer progressPercentage;

    private String participantsDisplay;

    private String durationDisplay;

    private String alarmStatus;

    public boolean hasParticipants() {
        return participants != null && !participants.isEmpty();
    }

    public boolean isParticipant(String employeeId) {
        return participantIds != null && participantIds.contains(employeeId);
    }

    public boolean isCreator(String employeeId) {
        return this.employeeId != null && this.employeeId.equals(employeeId);
    }

    public boolean isAccessibleBy(String employeeId) {
        // 작성자이거나 참여자이거나 공개 일정인 경우
        return isCreator(employeeId) ||
                isParticipant(employeeId) ||
                Visibility.PUBLIC.equals(visibility);
    }

    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && endDate != null &&
                !now.isBefore(startDate) && now.isBefore(endDate);
    }

    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && startDate.isAfter(now);
    }

    public boolean isPast() {
        LocalDateTime now = LocalDateTime.now();
        return endDate != null && endDate.isBefore(now);
    }

    public boolean isToday() {
        LocalDateTime now = LocalDateTime.now();
        return (startDate != null && startDate.toLocalDate().equals(now.toLocalDate())) ||
                (endDate != null && endDate.toLocalDate().equals(now.toLocalDate()));
    }

    public boolean isThisWeek() {
        if (startDate == null) return false;

        LocalDate now = LocalDate.now();
        LocalDate scheduleDate = startDate.toLocalDate();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return !scheduleDate.isBefore(startOfWeek) && !scheduleDate.isAfter(endOfWeek);
    }


    public long getDurationMinutes() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.Duration.between(startDate, endDate).toMinutes();
    }

    public double getDurationHours() {
        return getDurationMinutes() / 60.0;
    }

    public long getMinutesUntilStart() {
        if (startDate == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (startDate.isBefore(now)) return 0;

        return java.time.Duration.between(now, startDate).toMinutes();
    }

    public long getMinutesUntilEnd() {
        if (endDate == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (endDate.isBefore(now)) return 0;

        return java.time.Duration.between(now, endDate).toMinutes();
    }

    public int getProgressPercentage() {
        if (startDate == null || endDate == null) return 0;

        LocalDateTime now = LocalDateTime.now();

        // 아직 시작 안됨
        if (now.isBefore(startDate)) return 0;

        // 이미 끝남
        if (now.isAfter(endDate)) return 100;

        // 진행 중
        long totalDuration = java.time.Duration.between(startDate, endDate).toMinutes();
        long elapsed = java.time.Duration.between(startDate, now).toMinutes();

        return (int) ((elapsed * 100) / totalDuration);
    }


    public int getParticipantCount() {
        // 명시적으로 설정된 값이 있으면 우선 사용
        if (participantCount != null && participantCount >= 0) {
            return participantCount;
        }

        // 없으면 participants 리스트에서 계산
        return participants != null ? participants.size() : 0;
    }

    public String getParticipantsDisplay() {
        if (participants == null || participants.isEmpty()) {
            return "참여자 없음";
        }

        if (participants.size() <= 3) {
            return String.join(", ", participants);
        }

        return String.join(", ", participants.subList(0, 3)) + " 외 " + (participants.size() - 3) + "명";
    }

    public String getParticipantsSummary() {
        if (participants == null || participants.isEmpty()) {
            return "참여자 없음";
        }

        if (participants.size() == 1) {
            return participants.get(0);
        }

        return participants.get(0) + " 외 " + (participants.size() - 1) + "명";
    }


    public String getStartDateDisplay() {
        return startDate != null ? startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }

    public String getStartTimeDisplay() {
        return startDate != null ? startDate.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    public String getDateRangeDisplay() {
        if (startDate == null) return "";

        String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (endDate == null) return startDateStr;

        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        if (startLocalDate.equals(endLocalDate)) {
            return startDateStr; // 같은 날
        }

        return startDateStr + " ~ " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getTimeRangeDisplay() {
        if (startDate == null) return "";

        String startTimeStr = startDate.format(DateTimeFormatter.ofPattern("HH:mm"));

        if (endDate == null) return startTimeStr;

        return startTimeStr + " - " + endDate.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getDurationDisplay() {
        long minutes = getDurationMinutes();
        if (minutes == 0) return "시간 미정";

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "분";
        }

        if (remainingMinutes == 0) {
            return hours + "시간";
        }

        return hours + "시간 " + remainingMinutes + "분";
    }

    public String getStatus() {
        if (isPast()) return "PAST";
        if (isOngoing()) return "ONGOING";
        if (isUpcoming()) return "UPCOMING";
        return "UNKNOWN";
    }


    public long getMinutesUntilAlarm() {
        if (!hasAlarm()) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (alarmTime.isBefore(now)) return 0;

        return java.time.Duration.between(now, alarmTime).toMinutes();
    }

    public String getAlarmStatus() {
        if (!hasAlarm()) return "NONE";

        LocalDateTime now = LocalDateTime.now();
        if (alarmTime.isBefore(now)) return "SENT";

        return "SCHEDULED";
    }


    @JsonIgnore
    public boolean isValidSchedule() {
        return scheduleId != null && !scheduleId.trim().isEmpty() &&
                title != null && !title.trim().isEmpty() &&
                startDate != null && endDate != null &&
                startDate.isBefore(endDate);
    }

    @JsonIgnore
    public boolean isEditable() {
        // 과거 일정은 수정 불가
        return !isPast();
    }

    @JsonIgnore
    public boolean isDeletable() {
        // 진행 중이거나 과거 일정은 삭제 불가
        return !isOngoing() && !isPast();
    }

    @Override
    public String toString() {
        return String.format(
                "ScheduleResponseDTO{scheduleId='%s', title='%s', startDate=%s, participantCount=%d, status='%s'}",
                scheduleId, title, startDate, getParticipantCount(), getStatus()
        );
    }

    public String getSummary() {
        return String.format(
                "[%s] %s (%s) - %s, 참여자 %d명",
                scheduleId, title, getDateRangeDisplay(), getTimeRangeDisplay(), getParticipantCount()
        );
    }

    public static ScheduleResponseDTOBuilder createBasic(String scheduleId, String title) {
        return ScheduleResponseDTO.builder()
                .scheduleId(scheduleId)
                .title(title)
                .participantCount(0)
                .alarmEnabled(false);
    }

    public static ScheduleResponseDTO createSafe(String scheduleId, String title, String errorMessage) {
        return ScheduleResponseDTO.builder()
                .scheduleId(scheduleId != null ? scheduleId : "UNKNOWN")
                .title(title != null ? title : "오류: " + errorMessage)
                .description("데이터 로드 중 오류가 발생했습니다.")
                .participantCount(0)
                .alarmEnabled(false)
                .participants(List.of())
                .participantIds(List.of())
                .createdBy("알 수 없음")
                .build();
    }
}
package com.example.gagso.Schedules.dto;

import com.example.gagso.Schedules.models.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRegisterRequestDTO {


    private String employeeId;


    private String title;


    private String description;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;


    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;


    @Builder.Default
    private Boolean alarmEnabled = false;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    private List<String> participantIds;


    public boolean hasParticipants() {
        return participantIds != null && !participantIds.isEmpty();
    }


    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }


    public boolean isValidScheduleTime() {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }


    public boolean isFutureSchedule() {
        return startDate != null && startDate.isAfter(LocalDateTime.now());
    }

    public long getDurationMinutes() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.Duration.between(startDate, endDate).toMinutes();
    }


    public boolean isValidAlarmTime() {
        if (!hasAlarm()) {
            return true; // 알람이 없으면 유효
        }
        return alarmTime != null &&
                alarmTime.isAfter(LocalDateTime.now()) &&
                alarmTime.isBefore(startDate);
    }


    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                isValidScheduleTime() &&
                isFutureSchedule() &&
                isValidAlarmTime();
    }

    @Override
    public String toString() {
        return String.format("ScheduleRegisterRequestDTO{title='%s', startDate=%s, endDate=%s, alarmEnabled=%s}",
                title, startDate, endDate, alarmEnabled);
    }

    public LocalDateTime getStartDateTime() {
        return startDate;
    }

    public LocalDateTime getEndDateTime() {
        return endDate;
    }

    public List<String> getParticipants() {
        return participantIds;
    }
}
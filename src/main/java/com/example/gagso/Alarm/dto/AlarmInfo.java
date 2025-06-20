package com.example.gagso.Alarm.dto;

import com.example.gagso.Alarm.models.AlarmDomainType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmInfo {

    private String recipientPhone;

    private String targetId;

    private String title;
    private String message;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime noticeTime;

    private AlarmDomainType domainType;

    @Builder.Default
    private Boolean status = true;

    public static AlarmInfo forSchedule(String recipientPhone, String scheduleId,
                                        String title, String description, LocalDateTime noticeTime) {
        return AlarmInfo.builder()
                .recipientPhone(recipientPhone)
                .targetId(scheduleId)
                .title(title)
                .description(description)
                .noticeTime(noticeTime)
                .domainType(AlarmDomainType.SCHEDULE)
                .status(true)
                .build();
    }

    public static AlarmInfo forTask(String recipientPhone, String taskId,
                                    String title, String description, LocalDateTime noticeTime) {
        return AlarmInfo.builder()
                .recipientPhone(recipientPhone)
                .targetId(taskId)
                .title(title)
                .description(description)
                .noticeTime(noticeTime)
                .domainType(AlarmDomainType.TASK)
                .status(true)
                .build();
    }

    public boolean isValid() {
        return recipientPhone != null && !recipientPhone.trim().isEmpty()
                && targetId != null && !targetId.trim().isEmpty()
                && title != null && !title.trim().isEmpty()
                && noticeTime != null
                && domainType != null;
    }

    public boolean isFutureAlarm() {
        return noticeTime != null && noticeTime.isAfter(LocalDateTime.now());
    }
}
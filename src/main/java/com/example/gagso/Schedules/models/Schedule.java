package com.example.gagso.Schedules.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 일정 정보를 담고있는 엔티티 클래스
 * 설계 명세: DCD3001 - Entity Schedule
 * 지속성: Persistent
 */
@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @Column(name = "schedule_id", length = 36, nullable = false)
    private String scheduleId;

    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;


    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "start_date_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @Column(name = "end_date_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility;

    @Column(name = "alarm_enabled", nullable = false)
    @Builder.Default
    private Boolean alarmEnabled = false;

    @Column(name = "alarm_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startDate);
    }

    public boolean isFinished() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public long getDurationMinutes() {
        return java.time.Duration.between(startDate, endDate).toMinutes();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (alarmEnabled == null) {
            alarmEnabled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Schedule{id='%s', title='%s', startTime=%s, endTime=%s}",
                scheduleId, title, startDate, endDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Schedule schedule = (Schedule) obj;
        return scheduleId != null && scheduleId.equals(schedule.scheduleId);
    }

    @Override
    public int hashCode() {
        return scheduleId != null ? scheduleId.hashCode() : 0;
    }
}
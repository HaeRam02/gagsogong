package com.example.gagso.Schedules.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 일정 정보를 담는 엔티티 클래스
 * 설계 명세: DCD3001
 */
@Entity
@Table(name = "Schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @Column(name = "scheduleID", length = 36)
    private String scheduleId;

    @Column(name = "title", length = 20, nullable = false)
    private String title;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "startDateTime", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "endDateTime", nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility;

    @Column(name = "alarmEnabled", nullable = false)
    private Boolean alarmEnabled = false;

    @Column(name = "alarmTime")
    private LocalDateTime alarmTime;

    @Column(name = "employeeId", length = 50, nullable = false)
    private String employeeId;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    /**
     * 일정 ID 생성 (UUID 기반)
     */
    @PrePersist
    public void generateId() {
        if (this.scheduleId == null) {
            this.scheduleId = java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * 알림이 활성화된 일정인지 확인
     */
    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    /**
     * 현재 시간 기준으로 일정이 진행 중인지 확인
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDateTime) && now.isBefore(endDateTime);
    }

    /**
     * 현재 시간 기준으로 일정이 완료되었는지 확인
     */
    public boolean isCompleted() {
        return LocalDateTime.now().isAfter(endDateTime);
    }
}
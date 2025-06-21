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

    /**
     * 일정 고유 식별자 (Primary Key)
     */
    @Id
    @Column(name = "schedule_id", length = 36, nullable = false)
    private String scheduleId;

    /**
     * 일정 작성자 (직원 ID, 외래키)
     */
    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;

    /**
     * 일정 제목
     */
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    /**
     * 일정 설명
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 일정 시작 일시
     */
    @Column(name = "start_date_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    /**
     * 일정 종료 일시
     */
    @Column(name = "end_date_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    /**
     * 일정 공개 범위
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility;

    /**
     * 알람 설정 유무
     * @Builder.Default를 사용하여 기본값 설정
     */
    @Column(name = "alarm_enabled", nullable = false)
    @Builder.Default
    private Boolean alarmEnabled = false;

    /**
     * 알람 시간
     */
    @Column(name = "alarm_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    /**
     * 생성 시간
     */
    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 수정 시간
     */
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 비즈니스 메서드: 알람이 설정되어 있는지 확인
     */
    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    /**
     * 비즈니스 메서드: 일정이 현재 진행 중인지 확인
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    /**
     * 비즈니스 메서드: 일정이 미래 일정인지 확인
     */
    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startDate);
    }

    /**
     * 비즈니스 메서드: 일정이 종료되었는지 확인
     */
    public boolean isFinished() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * 비즈니스 메서드: 일정 기간 (분 단위)
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(startDate, endDate).toMinutes();
    }

    /**
     * JPA 콜백: 엔티티 저장 전 실행
     */
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

    /**
     * JPA 콜백: 엔티티 수정 전 실행
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * toString 메서드 (디버깅용)
     */
    @Override
    public String toString() {
        return String.format("Schedule{id='%s', title='%s', startTime=%s, endTime=%s}",
                scheduleId, title, startDate, endDate);
    }

    /**
     * equals & hashCode (scheduleId 기준)
     */
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
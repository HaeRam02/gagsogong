package com.example.gagso.Schedules.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * 일정 참여자 정보를 담는 엔티티 클래스
 * Schedule과 Employee 간의 다대다 관계를 표현
 * 설계 명세: DCD3002
 */
@Entity
@Table(name = "Participant",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"scheduleID", "employeeId"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @Column(name = "participant_id", length = 36, nullable = false)
    private String participantId;

    @Column(name = "scheduleID", length = 36, nullable = false)
    private String scheduleId;

    @Column(name = "employeeId", length = 50, nullable = false)
    private String employeeId;

    @CreationTimestamp
    @Column(name = "joinedAt", updatable = false)
    private LocalDateTime joinedAt;

    /**
     * 편의 생성자 - scheduleId와 employeeId로 생성
     */
    public static Participant of(String scheduleId, String employeeId) {
        return Participant.builder()
                .scheduleId(scheduleId)
                .employeeId(employeeId)
                .build();
    }
}
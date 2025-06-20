package com.example.gagso.Alarm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Alarm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alarm {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "recipientPhone", length = 20, nullable = false)
    private String recipientPhone;

    @Column(name = "targetId", length = 50, nullable = false)
    private String targetId;

    @Column(name = "title", length = 20, nullable = false)
    private String title;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "noticeTime", nullable = false)
    private LocalDateTime noticeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "domainType", nullable = false)
    private AlarmDomainType domainType;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Boolean status = true; // true: 활성, false: 비활성

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }

    public boolean isActive() {
        return status != null && status;
    }

    public boolean isFuture() {
        return noticeTime != null && noticeTime.isAfter(LocalDateTime.now());
    }

    public void deactivate() {
        this.status = false;
    }
}
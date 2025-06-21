package com.example.gagso.WorkRoom.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Task")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @Column(name = "taskId", length = 36, nullable = false, updatable = false)
    private String taskId;

    // JPA가 자동으로 taskId를 생성하도록 PrePersist 로직을 유지하는 것이 좋습니다.
    @PrePersist
    public void prePersist() {
        if (this.taskId == null) {
            this.taskId = java.util.UUID.randomUUID().toString();
        }
    }

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "isPublic", nullable = false)
    private boolean isPublic;

    @Column(name = "alarmEnabled", nullable = false)
    private boolean alarmEnabled;

    @Column(name = "attachment", length = 1000, nullable = true)
    private String attachment;

    @Column(name = "publicStartDate", nullable = true)
    private LocalDate publicStartDate;

    @Column(name = "publicEndDate", nullable = true)
    private LocalDate publicEndDate;

    @Column(name = "unitTask", length = 200, nullable = true)
    private String unitTask;

    @Column(name = "managerName", length = 100, nullable = false)
    private String managerName;

    @Column(name = "managerId", length = 36, nullable = false)
    private String managerId;

    @Column(name = "deptId", length = 36, nullable = false)
    private String deptId;
}
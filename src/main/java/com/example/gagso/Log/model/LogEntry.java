package com.example.gagso.Log.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "LogEntry")
@Getter
@Setter
@NoArgsConstructor
public class LogEntry {

    @Id
    @Column(name = "log_id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.timeStamp == null) {
            this.timeStamp = LocalDateTime.now();
        }
    }

    @Column(name = "actor_id", length = 20, nullable = false)
    private String actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 50, nullable = false)
    private ActionType actionType;

    @Column(name = "target_type", length = 20, nullable = false)
    private String targetType;

    @Column(name = "target_id", length = 100, nullable = false)
    private String targetId;

    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timeStamp;
}

package com.example.gagso.Clubs.models;

import com.example.gagso.Clubs.enums.Visibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "club") // ✅ 테이블명도 snake_case로
@Getter
@Setter
@NoArgsConstructor
public class Club {

    @Id
    @Column(name = "club_id", length = 36, nullable = false, updatable = false) // ✅ clubId → club_id
    private String clubId;

    @PrePersist
    public void prePersist() {
        if (this.clubId == null) {
            this.clubId = UUID.randomUUID().toString();
        }
    }

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "create_date", nullable = false) // ✅ createDate → create_date
    private LocalDateTime createDate;

    @Column(name = "member_count") // ✅ memberCount → member_count
    private int memberCount = 0;

    @Column(name = "creator_name", nullable = false) // ✅ creatorName → creator_name
    private String creatorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility;
}

package com.example.gagso.Clubs.dto;

import com.example.gagso.Clubs.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubRegisterRequestDTO {
    private String name;            // NOT NULL
    private String description;     // NULL 가능
    private LocalDateTime createDate; // NOT NULL (DATETIME)
    private int popularity;         // NOT NULL
    private Visibility visibility;  // NOT NULL (ENUM)
    private int memberCount;        // NOT NULL
    private String creatorName;     // NOT NULL
}
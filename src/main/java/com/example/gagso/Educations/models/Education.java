package com.example.gagso.Educations.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Education {

    @Id
    @Column(length = 36)
    private String educationId;

    private String title;

    private String educationType;

    private String instructor;

    private LocalDate applicationPeriodStart;
    private LocalDate applicationPeriodEnd;

    private LocalDate educationPeriodStart;
    private LocalDate educationPeriodEnd;

    private String attachmentType;
    private String attachmentPath;
}

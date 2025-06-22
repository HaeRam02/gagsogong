package com.example.gagso.Documents.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Document")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @Column(name = "docID", length = 36, nullable = false, updatable = false)
    private String docID;

    @Column(name = "writerID", length = 36, nullable = false)
    private String writerID;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    @Column(name = "visibility", nullable = false)
    private String visibility;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    // 자동 UUID 생성 (원한다면)
    @PrePersist
    public void prePersist() {
        if (this.docID == null) {
            this.docID = java.util.UUID.randomUUID().toString();
        }
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
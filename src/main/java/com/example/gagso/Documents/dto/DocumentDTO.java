package com.example.gagso.Documents.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.beans.Visibility;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private String docID;
    private String writerID;
    private String title;
    private String content;
    private String visibility;
    private LocalDate date;
    private String attachment;
}

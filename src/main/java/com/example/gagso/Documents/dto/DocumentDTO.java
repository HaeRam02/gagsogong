package com.example.gagso.Documents.dto;

import com.example.gagso.Documents.models.Attachment;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.beans.Visibility;
import java.time.LocalDate;
import java.util.List;

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
    private List<AttachmentDTO> attachments;
}

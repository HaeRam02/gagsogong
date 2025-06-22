package com.example.gagso.Documents.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AttachmentDTO {
    private Long id;
    private String originalName;
    private String path;   // 클라이언트에서 다운로드할 URL
    private long size;
}

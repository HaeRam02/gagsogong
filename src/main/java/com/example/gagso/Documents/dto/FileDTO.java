package com.example.gagso.Documents.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private String fileID;
    private String fileTitle;
    private String fileHistory;
}
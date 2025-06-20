package com.example.gagso.Educations.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EducationDto {

    private String educationId;

    private String title;
    private String instructor;

    @JsonProperty("educationType")
    private String education_type;

    @JsonProperty("applicationPeriodStart")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate application_period_start;

    @JsonProperty("applicationPeriodEnd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate application_period_end;

    @JsonProperty("educationPeriodStart")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate education_period_start;

    @JsonProperty("educationPeriodEnd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate education_period_end;

    @JsonProperty("attachmentType")
    private String attachment_type;

    @JsonProperty("attachmentPath")
    private String attachment_path; // 파일 업로드 시에는 실제 저장된 파일명
}

package com.example.gagso.WorkRoom.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isPublic;
    private LocalDate publicStartDate;
    private LocalDate publicEndDate;
    private boolean alarmEnabled;
    private String managerId;
    private String deptId;
    private String unitTask;
    private String managerName;
    private String creatorId;

}
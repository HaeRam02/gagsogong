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
public class TaskListItemDTO {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String managerName;
}

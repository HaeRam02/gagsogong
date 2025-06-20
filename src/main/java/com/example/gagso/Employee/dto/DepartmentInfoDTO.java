package com.example.gagso.Employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 부서 정보 전달용 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentInfoDTO {

    private String deptId;          // 부서 ID
    private String deptName;        // 부서명
    private String description;     // 부서 설명
    private int employeeCount;      // 부서 소속 직원 수

    @Override
    public String toString() {
        return String.format("%s (%d명)", deptName, employeeCount);
    }
}
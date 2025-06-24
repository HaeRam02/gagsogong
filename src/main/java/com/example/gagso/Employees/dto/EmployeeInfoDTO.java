package com.example.gagso.Employees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

/**
 * 직원 정보를 조회/표시용으로 담는 데이터 전달 객체 (비밀번호 제외)
 * 설계 명세: DCD1009 - DTO EmployeeInfoDTO
 * 지속성: Transient
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeInfoDTO {

    /**
     * 직원 ID
     */
    private String employeeId;

    /**
     * 직원 이름
     */
    private String name;

    /**
     * 부서 아이디
     */
    private String depId;

    /**
     * 부서 이름
     */
    private String depName;

    /**
     * 전화번호
     */
    @Pattern(regexp = "^(\\d{3})-(\\d{3,4})-(\\d{4})$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNum;

    // ⭐ 이 부분을 수정해야 합니다.
    public static EmployeeInfoDTO fromEmployee(com.example.gagso.Employees.models.Employee employee) {
        if (employee == null) {
            return null;
        }

        // ⭐ Employee 엔티티가 Department 객체를 가지고 있는지 확인하고 데이터를 가져옵니다.
        String departmentId = null;
        String departmentName = null;
        if (employee.getDepartment() != null) {
            departmentId = employee.getDepartment().getDeptId();
            departmentName = employee.getDepartment().getDeptTitle(); // Department 엔티티의 필드명 확인
        }

        return EmployeeInfoDTO.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .depId(departmentId)         // ⭐ Department 객체에서 depId 가져옴
                .depName(departmentName)     // ⭐ Department 객체에서 depName (deptTitle) 가져옴
                .phoneNum(employee.getPhoneNum())
                .build();
    }

    /**
     * 디버깅용 toString
     */
    @Override
    public String toString() {
        return String.format("EmployeeInfoDTO{employeeId='%s', name='%s', depId='%s', depName='%s', phoneNum='%s'}",
                employeeId, name, depId, depName, phoneNum);
    }
}
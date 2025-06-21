package com.example.gagso.Employees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

/**
 * 사용자가 입력한 직원 등록 정보를 담는 데이터 전달 객체
 * 설계 명세: DCD1009 - DTO EmployeeRegisterRequestDTO
 * 지속성: Transient
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRegisterRequestDTO {

    /**
     * 직원 ID
     */
    private String employeeId;

    /**
     * 직원 비밀번호
     */
    private String password;

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

    /**
     * 유효성 검사를 위한 헬퍼 메서드
     */
    public boolean isValid() {
        return employeeId != null && !employeeId.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() &&
               depId != null && !depId.trim().isEmpty() &&
               depName != null && !depName.trim().isEmpty() &&
               phoneNum != null && !phoneNum.trim().isEmpty();
    }

    /**
     * 디버깅용 toString (비밀번호 제외)
     */
    @Override
    public String toString() {
        return String.format("EmployeeRegisterRequestDTO{employeeId='%s', name='%s', depId='%s', depName='%s', phoneNum='%s'}",
                employeeId, name, depId, depName, phoneNum);
    }
}
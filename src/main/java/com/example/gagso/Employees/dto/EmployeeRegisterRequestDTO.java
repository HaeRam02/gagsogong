package com.example.gagso.Employees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRegisterRequestDTO {

    @NotBlank(message = "직원 ID는 필수입니다.")
    @Size(max = 20, message = "직원 ID는 최대 20자여야 합니다.")
    private String employeeId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(max = 20, message = "비밀번호는 최대 20자여야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 최대 50자여야 합니다.")
    private String name;

    @NotBlank(message = "부서 ID는 필수입니다.")
    private String depId; // depName 필드 제거됨

    @Pattern(regexp = "^(\\d{3})-(\\d{3,4})-(\\d{4})$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNum;

    @Override
    public String toString() {
        return String.format("EmployeeRegisterRequestDTO{employeeId='%s', name='%s', depId='%s', phoneNum='%s'}",
                employeeId, name, depId, phoneNum);
    }
}
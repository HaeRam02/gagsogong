package com.example.gagso.Employees.dto;

import com.example.gagso.Employees.models.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사용자가 입력한 직원 등록 결과(성공 여부, 오류 정보)를 포함하는 결과 객체
 * 설계 명세: ECD1013
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRegistrationResult {

    /**
     * 직원 등록 성공 여부
     */
    private boolean result;

    /**
     * 등록된 직원 정보 (성공 시에만 포함)
     */
    private Employee employee;

    /**
     * 각 입력 필드에 대한 유효성 검사 결과를 담는 구조화된 결과 객체
     */
    private ValidationResult validationResult;

    /**
     * 직원 등록 성공 여부를 확인하는 메서드
     */
    public boolean isSuccess() {
        return result && validationResult != null && validationResult.isSuccess();
    }

    /**
     * 등록된 직원을 반환하는 메서드
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * 유효성 검사 결과의 에러를 반환하는 메서드
     */
    public List<ValidationError> getErrors() {
        return validationResult != null ? validationResult.getErrors() : List.of();
    }

    /**
     * 직원 등록이 성공했을 때, 등록된 직원 객체를 포함한 결과 객체를 생성한다.
     */
    public static EmployeeRegistrationResult success(Employee employee) {
        return EmployeeRegistrationResult.builder()
                .result(true)
                .employee(employee)
                .validationResult(ValidationResult.ofSuccess())
                .build();
    }

    /**
     * 직원 등록이 유효성 검사에 실패했을 때, 오류 정보와 함께 실패 결과 객체를 생성한다.
     */
    public static EmployeeRegistrationResult failure(EmployeeRegisterRequestDTO employeeDTO, ValidationResult validationResult) {
        return EmployeeRegistrationResult.builder()
                .result(false)
                .employee(null)
                .validationResult(validationResult)
                .build();
    }

    /**
     * 직원 등록이 실패했을 때, 단일 오류 메시지와 함께 실패 결과 객체를 생성한다.
     */
    public static EmployeeRegistrationResult failure(String field, String message) {
        return EmployeeRegistrationResult.builder()
                .result(false)
                .employee(null)
                .validationResult(ValidationResult.ofFailure(field, message))
                .build();
    }
}
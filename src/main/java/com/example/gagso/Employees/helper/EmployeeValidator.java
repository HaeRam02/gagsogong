package com.example.gagso.Employees.helper;

import com.example.gagso.Employees.dto.EmployeeRegisterRequestDTO;
import com.example.gagso.Employees.dto.EmployeeRegistrationResult;
import com.example.gagso.Employees.dto.ValidationResult;
import com.example.gagso.Employees.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 직원 등록 요청에서 입력된 정보들을 검증하는 유효성 검사 클래스
 */
@Component
public class EmployeeValidator {

    private static final int MAX_ID_LENGTH = 20;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final int MAX_NAME_LENGTH = 20;
    private static final int MAX_DEP_ID_LENGTH = 20;
    private static final int MAX_DEP_NAME_LENGTH = 20;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 외부에서 호출하는 단일 진입점이고 내부 각 필드 검사를 실행한 후 결과를 반환
     */
    public EmployeeRegistrationResult validate(EmployeeRegisterRequestDTO reqEmployee) {
        ValidationResult result = ValidationResult.ofSuccess();

        // 각 검증 메서드 호출
        checkEmployeeId(reqEmployee, result);
        checkPassword(reqEmployee, result);
        checkName(reqEmployee, result);
        checkDepId(reqEmployee, result);
        checkDepName(reqEmployee, result);
        checkPhoneNum(reqEmployee, result);
        
        if (result.isSuccess()) {
            return EmployeeRegistrationResult.builder()
                    .result(true)
                    .validationResult(result)
                    .build();
        } else {
            return EmployeeRegistrationResult.failure(reqEmployee, result);
        }
    }

    /**
     * ID가 null 또는 공백인지, 길이를 초과하는지 검사
     */
    private void checkEmployeeId(EmployeeRegisterRequestDTO reqEmployee, ValidationResult result) {
        String employeeId = reqEmployee.getEmployeeId();

        if (!StringUtils.hasText(employeeId)) {
            result.addError("employeeId", "직원 ID는 필수 입력 항목입니다.");
            return;
        }

        if (employeeId.trim().length() > MAX_ID_LENGTH) {
            result.addError("employeeId", String.format("직원 ID는 %d자를 초과할 수 없습니다.", MAX_ID_LENGTH));
            return;
        }

        // 중복 검사
        if (employeeRepository.findByEmployeeId(employeeId).isPresent()) {
            result.addError("employeeId", "이미 존재하는 직원 ID입니다.");
        }
    }

    /**
     * Password가 null 또는 공백인지, 길이를 초과하는지 검사
     */
    private void checkPassword(EmployeeRegisterRequestDTO reqEmployee, ValidationResult result) {
        String password = reqEmployee.getPassword();

        if (!StringUtils.hasText(password)) {
            result.addError("password", "비밀번호는 필수 입력 항목입니다.");
            return;
        }

        if (password.trim().length() > MAX_PASSWORD_LENGTH) {
            result.addError("password", String.format("비밀번호는 %d자를 초과할 수 없습니다.", MAX_PASSWORD_LENGTH));
        }
    }

    /**
     * Name이 null 또는 공백인지, 길이를 초과하는지 검사
     */
    private void checkName(EmployeeRegisterRequestDTO reqEmployee, ValidationResult result) {
        String name = reqEmployee.getName();

        if (!StringUtils.hasText(name)) {
            result.addError("name", "이름은 필수 입력 항목입니다.");
            return;
        }

        if (name.trim().length() > MAX_NAME_LENGTH) {
            result.addError("name", String.format("이름은 %d자를 초과할 수 없습니다.", MAX_NAME_LENGTH));
        }
    }

    /**
     * 부서 ID가 null 또는 공백인지, 길이를 초과하는지 검사
     */
    private void checkDepId(EmployeeRegisterRequestDTO reqEmployee, ValidationResult result) {
        String depId = reqEmployee.getDepId();

        if (!StringUtils.hasText(depId)) {
            result.addError("depId", "부서 ID는 필수 입력 항목입니다.");
            return;
        }

        if (depId.trim().length() > MAX_DEP_ID_LENGTH) {
            result.addError("depId", String.format("부서 ID는 %d자를 초과할 수 없습니다.", MAX_DEP_ID_LENGTH));
        }
    }

    /**
     * 부서명이 null 또는 공백인지, 길이를 초과하는지 검사
     */
    private void checkDepName(EmployeeRegisterRequestDTO reqEmployee, ValidationResult result) {
        String depName = reqEmployee.getDepName();

        if (!StringUtils.hasText(depName)) {
            result.addError("depName", "부서명은 필수 입력 항목입니다.");
            return;
        }

        if (depName.trim().length() > MAX_DEP_NAME_LENGTH) {
            result.addError("depName", String.format("부서명은 %d자를 초과할 수 없습니다.", MAX_DEP_NAME_LENGTH));
        }
    }

    /**
     * 전화번호가 null 또는 공백인지, 형식이 올바른지 검사
     */
    private void checkPhoneNum(EmployeeRegisterRequestDTO reqEmployee, ValidationResult result) {
        String phoneNum = reqEmployee.getPhoneNum();

        if (!StringUtils.hasText(phoneNum)) {
            result.addError("phoneNum", "전화번호는 필수 입력 항목입니다.");
            return;
        }

        // 전화번호 형식 검사
        String phonePattern = "^(\\d{3})-(\\d{3,4})-(\\d{4})$";
        if (!phoneNum.matches(phonePattern)) {
            result.addError("phoneNum", "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
            return;
        }

        // 중복 검사
        if (employeeRepository.findByPhoneNum(phoneNum).isPresent()) {
            result.addError("phoneNum", "이미 등록된 전화번호입니다.");
        }
    }
}
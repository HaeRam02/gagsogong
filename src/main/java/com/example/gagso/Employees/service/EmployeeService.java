package com.example.gagso.Employees.service;

import com.example.gagso.Department.models.Department;
import com.example.gagso.Department.repository.DepartmentRepository;
import com.example.gagso.Employees.dto.EmployeeInfoDTO; // EmployeeInfoDTO 임포트
import com.example.gagso.Employees.dto.EmployeeRegisterRequestDTO;
import com.example.gagso.Employees.dto.EmployeeRegistrationResult;
import com.example.gagso.Employees.helper.EmployeeValidator;
import com.example.gagso.Employees.models.Employee;
import com.example.gagso.Employees.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gagso.Log.service.EmployeeLogWriter;
import com.example.gagso.Log.model.ActionType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 직원 등록, 조회의 비즈니스 로직을 처리한다.
 * 저장소 접근, 유효성 검사, 로그 기록 등을 통합 조절한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // 모든 조회 메서드에 기본적으로 적용, 쓰기 메서드에만 @Transactional 별도 선언
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeValidator validator;
    private final DepartmentRepository departmentRepository;
    private final EmployeeLogWriter employeeLogWriter; // 이 줄을 추가

    // PasswordEncoder는 사용하지 않으므로 주입 제거

    /**
     * 직원 등록, 유효성 검사, 로그 기록, 저장 요청
     * 직원 등록 결과를 반환하는 직원 등록 전체 흐름
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public EmployeeRegistrationResult register(EmployeeRegisterRequestDTO employeeDTO) {
        // 1. 유효성 검사
        EmployeeRegistrationResult validationResult = validator.validate(employeeDTO);
        if (!validationResult.isSuccess()) {
            log.warn("직원 등록 유효성 검사 실패: {}", validationResult.getErrors());
            return validationResult;
        }

        try {
            // 2. DTO를 엔티티로 변환
            Employee employee = convertToEmployee(employeeDTO);

            // 3. 직원 저장
            Employee savedEmployee = employeeRepository.save(employee);
            log.info("직원 저장 완료: {}", savedEmployee.getEmployeeId());
            employeeLogWriter.save(savedEmployee.getEmployeeId(), ActionType.REGISTER, savedEmployee);

            return EmployeeRegistrationResult.success(savedEmployee);

        } catch (IllegalArgumentException e) { // 유효하지 않은 부서 ID 같은 특정 예외 처리
            log.warn("직원 등록 실패: {}", e.getMessage());
            // 프론트엔드에 특정 필드 오류임을 알리기 위해 "depId"와 메시지 전달
            return EmployeeRegistrationResult.failure("depId", e.getMessage());
        } catch (Exception e) {
            log.error("직원 등록 중 오류 발생", e);
            return EmployeeRegistrationResult.failure("system", "직원 등록 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * Employee 엔티티를 EmployeeInfoDTO로 변환하는 헬퍼 메서드
     * null 체크를 통해 NullPointerException 방지
     */
    private EmployeeInfoDTO convertToEmployeeInfoDTO(Employee employee) {
        String depId = null;
        String depName = null;
        // Department 객체가 null이 아닌 경우에만 부서 정보 추출
        if (employee.getDepartment() != null) {
            depId = employee.getDepartment().getDeptId();
            depName = employee.getDepartment().getDeptTitle(); // Department 엔티티의 부서명 필드명 확인 (deptTitle)
        }

        return EmployeeInfoDTO.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .depId(depId)
                .depName(depName)
                .phoneNum(employee.getPhoneNum())
                .build();
    }

    /**
     * 전체 직원 조회 (DTO 반환으로 변경)
     */
    // @Transactional(readOnly = true)는 클래스 레벨에 선언되어 있으므로 여기서 중복 제거
    public List<EmployeeInfoDTO> getAllEmployeesInfo() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            log.info("전체 직원 조회 완료: {} 명", employees.size());
            // Employee 리스트를 DTO 리스트로 변환
            return employees.stream()
                    .map(this::convertToEmployeeInfoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("전체 직원 조회 중 오류 발생", e);
            return List.of(); // 빈 리스트 반환
        }
    }

    /**
     * 직원 ID로 개별 직원 조회 (DTO 반환으로 변경)
     */
    public EmployeeInfoDTO getEmployeeInfoById(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .map(this::convertToEmployeeInfoDTO) // DTO로 변환
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다: " + employeeId));
    }

    /**
     * 이름으로 직원 조회 (DTO 반환으로 변경)
     */
    public List<EmployeeInfoDTO> getEmployeeInfoByName(String name) {
        try {
            List<Employee> employees = employeeRepository.findByName(name);
            log.info("이름별 직원 조회 완료: 이름 '{}', {} 명", name, employees.size());
            // DTO 리스트로 변환
            return employees.stream()
                    .map(this::convertToEmployeeInfoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("이름별 직원 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 부서 ID로 직원 조회 (DTO 반환으로 변경)
     */
    public List<EmployeeInfoDTO> getEmployeeInfoByDepId(String depId) {
        try {
            List<Employee> employees = employeeRepository.findByDepartment_DeptId(depId);
            log.info("부서별 직원 조회 완료: 부서 ID '{}', {} 명", depId, employees.size());
            // DTO 리스트로 변환
            return employees.stream()
                    .map(this::convertToEmployeeInfoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("부서별 직원 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 부서명으로 직원 조회 (DTO 반환으로 변경)
     */
    public List<EmployeeInfoDTO> getEmployeesInfoByDepName(String depName) {
        try {
            List<Employee> employees = employeeRepository.findByDepartment_DeptTitle(depName);
            log.info("부서명별 직원 조회 완료: 부서명 '{}', {} 명", depName, employees.size());
            // DTO 리스트로 변환
            return employees.stream()
                    .map(this::convertToEmployeeInfoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("부서명별 직원 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 전화번호로 직원 조회 (DTO 반환으로 변경)
     */
    public EmployeeInfoDTO getEmployeeInfoByPhoneNum(String phoneNum) {
        return employeeRepository.findByPhoneNum(phoneNum)
                .map(this::convertToEmployeeInfoDTO) // DTO로 변환
                .orElseThrow(() -> new IllegalArgumentException("해당 전화번호의 직원을 찾을 수 없습니다: " + phoneNum));
    }

    /**
     * 직원 삭제
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public void deleteEmployee(String employeeId) {
        try {
            // 삭제할 직원이 존재하는지 먼저 확인
            if (!employeeRepository.existsById(employeeId)) {
                throw new IllegalArgumentException("삭제할 직원을 찾을 수 없습니다: " + employeeId);
            }
            employeeRepository.deleteById(employeeId); // ID를 사용하여 삭제
            log.info("직원 삭제 완료: {}", employeeId);
        } catch (IllegalArgumentException e) {
            throw e; // 특정 예외는 컨트롤러로 다시 던져서 404 Not Found 등으로 처리
        } catch (Exception e) {
            log.error("직원 삭제 중 오류 발생", e);
            throw new RuntimeException("직원 삭제 중 시스템 오류가 발생했습니다."); // 일반 예외는 런타임 예외로 감싸서 던짐
        }
    }

    /**
     * 직원 ID 존재 여부 확인
     */
    public boolean existsByEmployeeId(String employeeId) {
        return employeeRepository.existsByEmployeeId(employeeId);
    }

    /**
     * 전화번호 존재 여부 확인
     */
    public boolean existsByPhoneNum(String phoneNum) {
        return employeeRepository.existsByPhoneNum(phoneNum);
    }

    /**
     * DTO를 Employee 엔티티로 변환
     * PasswordEncoder를 사용하지 않으므로 비밀번호는 그대로 설정
     */
    private Employee convertToEmployee(EmployeeRegisterRequestDTO employeeDTO) {
        // EmployeeValidator에서 이미 유효성을 검사했더라도, Service 계층에서 Department 엔티티를 직접 찾아 연결
        Department department = departmentRepository.findById(employeeDTO.getDepId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 부서 ID: " + employeeDTO.getDepId()));

        return Employee.builder()
                .employeeId(employeeDTO.getEmployeeId())
                .password(employeeDTO.getPassword()) // PasswordEncoder를 사용하지 않음
                .name(employeeDTO.getName())
                .phoneNum(employeeDTO.getPhoneNum())
                .department(department) // 조회한 Department 객체를 Employee에 설정
                .build();
    }

    /**
     * 부서별 통계 정보를 담는 내부 클래스 (현재 사용되지는 않지만 유지)
     */
    public static class DepartmentStatistics {
        private final String departmentName;
        private final long employeeCount;

        public DepartmentStatistics(String departmentName, long employeeCount) {
            this.departmentName = departmentName;
            this.employeeCount = employeeCount;
        }

        public String getDepartmentName() { return departmentName; }
        public long getEmployeeCount() { return employeeCount; }
    }
}
package com.example.gagso.Employees.service;

import com.example.gagso.Employees.dto.EmployeeRegisterRequestDTO;
import com.example.gagso.Employees.dto.EmployeeRegistrationResult;
import com.example.gagso.Employees.helper.EmployeeValidator;
import com.example.gagso.Employees.models.Employee;
import com.example.gagso.Employees.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 직원 등록, 조회의 비즈니스 로직을 처리한다.
 * 저장소 접근, 유효성 검사, 로그 기록 등을 통합 조절한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeValidator validator;

    /**
     * Repository 직접 접근을 위한 Getter (EmployeeController에서 사용)
     */
    public EmployeeRepository getEmployeeRepository() {
        return employeeRepository;
    }

    /**
     * 직원 등록, 유효성 검사, 로그 기록, 저장 요청
     * 직원 등록 결과를 반환하는 직원 등록 전체 흐름
     */
    @Transactional
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
            Employee savedEmployee = employeeRepository.register(employee);
            log.info("직원 저장 완료: {}", savedEmployee.getEmployeeId());

            return EmployeeRegistrationResult.success(savedEmployee);

        } catch (Exception e) {
            log.error("직원 등록 중 오류 발생", e);
            return EmployeeRegistrationResult.failure("system", "직원 등록 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 전체 직원 조회
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        try {
            List<Employee> employees = employeeRepository.findAllByOrderByDepNameAscNameAsc();
            log.info("전체 직원 조회 완료: {} 명", employees.size());
            return employees;
        } catch (Exception e) {
            log.error("전체 직원 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 직원 ID로 개별 직원 조회
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다: " + employeeId));
    }



    /**
     * 이름으로 직원 조회
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeeByName(String name) {
        try {
            List<Employee> employees = employeeRepository.findByName(name);
            log.info("이름별 직원 조회 완료: 이름 '{}', {} 명", name, employees.size());
            return employees;
        } catch (Exception e) {
            log.error("이름별 직원 조회 중 오류 발생", e);
            return List.of();
        }
    }


    /**
     * 부서 ID로 직원 조회
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeeByDepId(String depId) {
        try {
            List<Employee> employees = employeeRepository.findByDepId(depId);
            log.info("부서별 직원 조회 완료: 부서 ID '{}', {} 명", depId, employees.size());
            return employees;
        } catch (Exception e) {
            log.error("부서별 직원 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 부서명으로 직원 조회
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepName(String depName) {
        try {
            List<Employee> employees = employeeRepository.findByDepName(depName);
            log.info("부서명별 직원 조회 완료: 부서명 '{}', {} 명", depName, employees.size());
            return employees;
        } catch (Exception e) {
            log.error("부서명별 직원 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 전화번호로 직원 조회
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeByPhoneNum(String phoneNum) {
        return employeeRepository.findByPhoneNum(phoneNum)
                .orElseThrow(() -> new IllegalArgumentException("해당 전화번호의 직원을 찾을 수 없습니다: " + phoneNum));
    }



    /**
     * 직원 삭제
     */
    @Transactional
    public void deleteEmployee(String employeeId) {
        try {
            Employee employee = getEmployeeById(employeeId);
            employeeRepository.delete(employee);
            log.info("직원 삭제 완료: {}", employeeId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("직원 삭제 중 오류 발생", e);
            throw new RuntimeException("직원 삭제 중 시스템 오류가 발생했습니다.");
        }
    }



    /**
     * 직원 ID 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return employeeRepository.existsByEmployeeId(employeeId);
    }

    /**
     * 전화번호 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByPhoneNum(String phoneNum) {
        return employeeRepository.existsByPhoneNum(phoneNum);
    }

    /**
     * DTO를 Employee 엔티티로 변환
     */
    private Employee convertToEmployee(EmployeeRegisterRequestDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeDTO.getEmployeeId());
        employee.setPassword(employeeDTO.getPassword());
        employee.setName(employeeDTO.getName());
        employee.setDepId(employeeDTO.getDepId());
        employee.setDepName(employeeDTO.getDepName());
        employee.setPhoneNum(employeeDTO.getPhoneNum());

        log.debug("DTO를 Employee 엔티티로 변환 완료: {}", employee.getEmployeeId());
        return employee;
    }

    /**
     * 수정용 유효성 검사 (기존 데이터 제외)
     */
    private EmployeeRegistrationResult validateForUpdate(EmployeeRegisterRequestDTO employeeDTO, Employee existingEmployee) {
        // 기본 유효성 검사는 validator에서 수행하되, 중복 검사만 따로 처리
        // 실제로는 validator를 수정하거나 별도 로직 필요
        return EmployeeRegistrationResult.success(existingEmployee);
    }

    /**
     * 직원 정보 필드 업데이트
     */
    private void updateEmployeeFields(Employee employee, EmployeeRegisterRequestDTO dto) {
        employee.setPassword(dto.getPassword());
        employee.setName(dto.getName());
        employee.setDepId(dto.getDepId());
        employee.setDepName(dto.getDepName());
        employee.setPhoneNum(dto.getPhoneNum());
    }

    /**
     * 부서별 통계 정보를 담는 내부 클래스
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
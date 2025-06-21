package com.example.gagso.Employees.service;

import com.example.gagso.Employees.dto.EmployeeInfoDTO;
import com.example.gagso.Employees.models.Employee;
import com.example.gagso.Employees.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 다른 서브시스템에서 직원 정보 조회 요청 시 제공하는 서비스 구현체
 * EmployeeInfoProvider 인터페이스의 구현 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // 조회 전용이므로 readOnly 설정
public class EmployeeInfoProviderImpl implements EmployeeInfoProvider {

    private final EmployeeRepository employeeRepository;

    /**
     * 모든 직원의 기본 정보를 조회합니다.
     * @return 모든 직원의 기본 정보 리스트
     */
    @Override
    public List<EmployeeInfoDTO> getAllBasicInfo() {
        log.info("모든 직원 기본 정보 조회 요청");

        try {
            List<Employee> employees = employeeRepository.findAll();
            log.info("총 {}명의 직원 정보를 조회했습니다.", employees.size());

            return employees.stream()
                    .map(EmployeeInfoDTO::fromEmployee)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("모든 직원 정보 조회 중 오류 발생", e);
            throw new RuntimeException("직원 정보 조회에 실패했습니다.", e);
        }
    }

    /**
     * 특정 부서의 직원 정보를 조회합니다.
     * @param deptId 부서 ID
     * @return 해당 부서 직원 정보 리스트
     */
    @Override
    public List<EmployeeInfoDTO> getEmployeeByDept(String deptId) {
        log.info("부서 ID로 직원 조회 요청: {}", deptId);

        if (deptId == null || deptId.trim().isEmpty()) {
            log.warn("부서 ID가 비어있습니다.");
            throw new IllegalArgumentException("부서 ID는 필수입니다.");
        }

        try {
            List<Employee> employees = employeeRepository.findByDepId(deptId.trim());
            log.info("부서 ID '{}': {}명의 직원을 조회했습니다.", deptId, employees.size());

            return employees.stream()
                    .map(EmployeeInfoDTO::fromEmployee)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("부서별 직원 정보 조회 중 오류 발생. 부서 ID: {}", deptId, e);
            throw new RuntimeException("부서별 직원 정보 조회에 실패했습니다.", e);
        }
    }

    /**
     * 특정 직원의 상세 정보를 조회합니다.
     * @param employeeId 직원 ID
     * @return 직원 상세 정보 (없으면 null)
     */
    @Override
    public EmployeeInfoDTO getEmployeeInfo(String employeeId) {
        log.info("직원 ID로 상세 정보 조회 요청: {}", employeeId);

        if (employeeId == null || employeeId.trim().isEmpty()) {
            log.warn("직원 ID가 비어있습니다.");
            throw new IllegalArgumentException("직원 ID는 필수입니다.");
        }

        try {
            Employee employee = employeeRepository.findById(employeeId.trim()).orElse(null);

            if (employee == null) {
                log.warn("직원 ID '{}'에 해당하는 직원을 찾을 수 없습니다.", employeeId);
                return null;
            }

            log.info("직원 정보 조회 성공: {} ({})", employee.getName(), employeeId);
            return EmployeeInfoDTO.fromEmployee(employee);

        } catch (Exception e) {
            log.error("직원 정보 조회 중 오류 발생. 직원 ID: {}", employeeId, e);
            throw new RuntimeException("직원 정보 조회에 실패했습니다.", e);
        }
    }

    /**
     * 직원 이름으로 직원 정보를 조회합니다.
     * @param name 직원 이름
     * @return 해당 이름의 직원 정보 리스트
     */
    @Override
    public List<EmployeeInfoDTO> getEmployeeByName(String name) {
        log.info("직원 이름으로 조회 요청: {}", name);

        if (name == null || name.trim().isEmpty()) {
            log.warn("직원 이름이 비어있습니다.");
            throw new IllegalArgumentException("직원 이름은 필수입니다.");
        }

        try {
            List<Employee> employees = employeeRepository.findByName(name.trim());
            log.info("이름 '{}': {}명의 직원을 조회했습니다.", name, employees.size());

            return employees.stream()
                    .map(EmployeeInfoDTO::fromEmployee)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("이름별 직원 정보 조회 중 오류 발생. 이름: {}", name, e);
            throw new RuntimeException("이름별 직원 정보 조회에 실패했습니다.", e);
        }
    }

    /**
     * DTO를 Employee 엔티티로 변환하는 헬퍼 메서드
     * @param employee Employee 엔티티
     * @return EmployeeInfoDTO
     */
    private EmployeeInfoDTO convertToDTO(Employee employee) {
        return EmployeeInfoDTO.fromEmployee(employee);
    }
}
// EmployeeInfoProviderImpl.java - 완전히 새로운 파일로 교체
package com.example.gagso.Employee.service;

import com.example.gagso.Employee.dto.EmployeeInfoDTO;
import com.example.gagso.Employee.models.Employee;
import com.example.gagso.Employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 직원 정보를 요청받아 비즈니스 로직에 따라 처리하고 제공하는 구현 클래스
 * 설계 명세: DCD1003 - EmployeeInfoProviderImpl
 * 지속성: Transient
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeInfoProviderImpl implements EmployeeInfoProvider {

    private final EmployeeRepository employeeRepository;

    /**
     * 모든 직원의 기본 정보를 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getAllBasicInfo() -> List<EmployeeInfoDTO>
     */
    @Override
    public List<EmployeeInfoDTO> getAllBasicInfo() {
        log.info("전체 직원 기본 정보 조회 요청");

        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeInfoDTO> result = employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("전체 직원 기본 정보 조회 완료: {} 명", result.size());
        return result;
    }

    /**
     * 지정된 부서에 속한 직원 목록을 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getEmployeeByDept(depId: String) -> List<EmployeeInfoDTO>
     */
    @Override
    public List<EmployeeInfoDTO> getEmployeeByDept(String deptId) {
        log.info("부서별 직원 조회 요청: deptId={}", deptId);

        List<Employee> employees = employeeRepository.findEmployeeByDepId(deptId);
        List<EmployeeInfoDTO> result = employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("부서별 직원 조회 완료: deptId={}, 직원 수={}", deptId, result.size());
        return result;
    }

    /**
     * 지정된 직원 ID에 해당하는 직원의 상세 정보를 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getEmployeeInfo(employeeId: String) -> Employee
     * 주의: Optional이 아닌 Employee 직접 반환, 없으면 null 반환
     */
    @Override
    public Employee getEmployeeInfo(String employeeId) {
        log.info("직원 상세 정보 조회 요청: employeeId={}", employeeId);

        Optional<Employee> employee = employeeRepository.findByEmployeeId(employeeId);

        if (employee.isPresent()) {
            log.info("직원 상세 정보 조회 성공: employeeId={}, name={}",
                    employeeId, employee.get().getName());
            return employee.get();
        } else {
            log.warn("직원 상세 정보 조회 실패: employeeId={}", employeeId);
            return null;
        }
    }

    /**
     * 지정된 이름의 직원 목록을 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getEmployeeByName(name: String) -> List<EmployeeInfoDTO>
     */
    @Override
    public List<EmployeeInfoDTO> getEmployeeByName(String name) {
        log.info("이름별 직원 조회 요청: name={}", name);

        List<Employee> employees = employeeRepository.findEmployeeByName(name);
        List<EmployeeInfoDTO> result = employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("이름별 직원 조회 완료: name={}, 직원 수={}", name, result.size());
        return result;
    }

    /**
     * 키워드로 직원 검색
     */
    @Override
    public List<EmployeeInfoDTO> searchEmployees(String keyword) {
        log.info("키워드 검색 요청: keyword={}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBasicInfo();
        }

        List<Employee> employees = employeeRepository.searchByKeyword(keyword.trim());
        List<EmployeeInfoDTO> result = employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("키워드 검색 완료: keyword={}, 결과 수={}", keyword, result.size());
        return result;
    }

    /**
     * 직원 존재 여부 확인
     */
    @Override
    public boolean existsById(String employeeId) {
        boolean exists = employeeRepository.findByEmployeeId(employeeId).isPresent();
        log.debug("직원 존재 여부 확인: employeeId={}, exists={}", employeeId, exists);
        return exists;
    }

    /**
     * 두 직원이 같은 부서인지 확인
     */
    @Override
    public boolean isSameDepartment(String employeeId1, String employeeId2) {
        boolean sameDept = employeeRepository.isSameDepartment(employeeId1, employeeId2);

        log.debug("같은 부서 여부 확인: employeeId1={}, employeeId2={}, result={}",
                employeeId1, employeeId2, sameDept);
        return sameDept;
    }

    /**
     * Entity에서 받아온 정보를 DTO로 변환시켜주는 메서드
     * 설계 명세: convertToDTO(Employee: employee) -> EmployeeInfoDTO
     */
    private EmployeeInfoDTO convertToDTO(Employee employee) {
        return EmployeeInfoDTO.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .deptId(employee.getDeptId())
                .deptName(employee.getDeptName())
                .phoneNumber(employee.getPhoneNum())
                .build();
    }

    /**
     * 부서별 통계 조회
     */
    public List<Object[]> getDepartmentStatistics() {
        log.info("부서별 통계 조회 요청");
        return employeeRepository.countEmployeesByDepartment();
    }

    /**
     * 사용자 ID로 직원 조회 (로그인 시 사용) - Optional 래핑하여 반환
     */
    public Optional<Employee> findByUserId(String userId) {
        return employeeRepository.findByUserId(userId);
    }

    /**
     * getEmployeeInfo와 동일하지만 Optional 래핑하여 반환 (편의 메서드)
     */
    public Optional<Employee> getEmployeeById(String employeeId) {
        Employee employee = getEmployeeInfo(employeeId);
        return Optional.ofNullable(employee);
    }
}
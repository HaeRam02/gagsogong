// DummyEmployeeService.java - 완전히 새로운 파일로 교체
package com.example.gagso.Employee.service;

import com.example.gagso.Employee.dto.EmployeeInfoDTO;
import com.example.gagso.Employee.models.Employee;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Employee 더미 데이터를 제공하는 서비스
 * EmployeeInfoProvider 인터페이스 구현
 * 실제 DB 연동 대신 하드코딩된 더미 데이터 사용
 *
 * ET-01 Employee 테이블 명세에 따른 핵심 필드만 포함:
 * - employeeId, userId, password, name, deptId, deptName, phoneNum
 */
@Service
public class DummyEmployeeService implements EmployeeInfoProvider {

    // 더미 Employee 데이터 (설계서의 팀원들 + 추가 직원들)
    private static final List<Employee> DUMMY_EMPLOYEES = Arrays.asList(
            Employee.builder()
                    .employeeId("EMP001")
                    .userId("kim.donghyun")
                    .password("password123") // 실제로는 암호화된 비밀번호
                    .name("김동현")
                    .deptId("DEPT001")
                    .deptName("개발팀")
                    .phoneNum("010-1234-5678")
                    .build(),
            Employee.builder()
                    .employeeId("EMP002")
                    .userId("kang.yunjae")
                    .password("password123")
                    .name("강윤제")
                    .deptId("DEPT001")
                    .deptName("개발팀")
                    .phoneNum("010-2345-6789")
                    .build(),
            Employee.builder()
                    .employeeId("EMP003")
                    .userId("koo.gayeon")
                    .password("password123")
                    .name("구가연")
                    .deptId("DEPT001")
                    .deptName("개발팀")
                    .phoneNum("010-3456-7890")
                    .build(),
            Employee.builder()
                    .employeeId("EMP004")
                    .userId("sung.hyeram")
                    .password("password123")
                    .name("성혜람")
                    .deptId("DEPT002")
                    .deptName("기획팀")
                    .phoneNum("010-4567-8901")
                    .build(),
            Employee.builder()
                    .employeeId("EMP005")
                    .userId("jang.seyeon")
                    .password("password123")
                    .name("장세연")
                    .deptId("DEPT002")
                    .deptName("기획팀")
                    .phoneNum("010-5678-9012")
                    .build(),
            Employee.builder()
                    .employeeId("EMP006")
                    .userId("choi.horim")
                    .password("password123")
                    .name("최호림")
                    .deptId("DEPT003")
                    .deptName("인사팀")
                    .phoneNum("010-6789-0123")
                    .build(),
            Employee.builder()
                    .employeeId("EMP007")
                    .userId("hong.gildong")
                    .password("password123")
                    .name("홍길동")
                    .deptId("DEPT001")
                    .deptName("개발팀")
                    .phoneNum("010-7890-1234")
                    .build(),
            Employee.builder()
                    .employeeId("EMP008")
                    .userId("lee.younghee")
                    .password("password123")
                    .name("이영희")
                    .deptId("DEPT002")
                    .deptName("기획팀")
                    .phoneNum("010-8901-2345")
                    .build()
    );

    @Override
    public List<EmployeeInfoDTO> getAllBasicInfo() {
        return DUMMY_EMPLOYEES.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeInfoDTO> getEmployeeByDept(String deptId) {
        return DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getDeptId().equals(deptId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 설계 명세: getEmployeeInfo(employeeId: String) -> Employee
     * 주의: Optional이 아닌 Employee 직접 반환, 없으면 null 반환
     */
    @Override
    public Employee getEmployeeInfo(String employeeId) {
        return DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getEmployeeId().equals(employeeId))
                .findFirst()
                .orElse(null); // 설계 명세에 따라 null 반환
    }

    @Override
    public List<EmployeeInfoDTO> getEmployeeByName(String name) {
        return DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getName().contains(name))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeInfoDTO> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBasicInfo();
        }

        String lowerKeyword = keyword.toLowerCase();
        return DUMMY_EMPLOYEES.stream()
                .filter(emp ->
                        emp.getName().toLowerCase().contains(lowerKeyword) ||
                                emp.getDeptName().toLowerCase().contains(lowerKeyword) ||
                                emp.getUserId().toLowerCase().contains(lowerKeyword)
                )
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String employeeId) {
        return getEmployeeInfo(employeeId) != null;
    }

    @Override
    public boolean isSameDepartment(String employeeId1, String employeeId2) {
        Employee emp1 = getEmployeeInfo(employeeId1);
        Employee emp2 = getEmployeeInfo(employeeId2);

        if (emp1 != null && emp2 != null) {
            return emp1.getDeptId().equals(emp2.getDeptId());
        }
        return false;
    }

    // 편의 메서드들 (기존 코드와의 호환성을 위해)

    /**
     * 모든 Employee 엔티티 반환
     */
    public List<Employee> getAllEmployees() {
        return DUMMY_EMPLOYEES;
    }

    /**
     * getEmployeeInfo와 동일하지만 Optional 래핑하여 반환 (편의 메서드)
     */
    public Optional<Employee> getEmployeeById(String employeeId) {
        Employee employee = getEmployeeInfo(employeeId);
        return Optional.ofNullable(employee);
    }

    /**
     * 사용자 ID로 직원 조회
     */
    public Optional<Employee> findByUserId(String userId) {
        return DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 부서 ID로 Employee 엔티티 목록 조회
     */
    public List<Employee> getEmployeesByDeptId(String deptId) {
        return DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getDeptId().equals(deptId))
                .collect(Collectors.toList());
    }

    /**
     * 부서별 통계 조회
     */
    public List<Object[]> getDepartmentStatistics() {
        return DUMMY_EMPLOYEES.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDeptName,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());
    }

    /**
     * Employee를 EmployeeInfoDTO로 변환
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
     * 새 직원 ID 생성 (더미용)
     */
    public String generateNewEmployeeId() {
        int maxId = DUMMY_EMPLOYEES.stream()
                .mapToInt(emp -> Integer.parseInt(emp.getEmployeeId().substring(3)))
                .max()
                .orElse(0);
        return String.format("EMP%03d", maxId + 1);
    }

    /**
     * 직원 수 조회
     */
    public long getEmployeeCount() {
        return DUMMY_EMPLOYEES.size();
    }

    /**
     * 부서별 직원 수 조회
     */
    public long getEmployeeCountByDept(String deptId) {
        return DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getDeptId().equals(deptId))
                .count();
    }
}
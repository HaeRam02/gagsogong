package com.example.gagso.Employee.service;

import com.example.gagso.Employee.dto.EmployeeInfoDTO;
import com.example.gagso.Employee.models.Employee;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Employee 더미 데이터를 제공하는 서비스
 * EmployeeInfoProvider 인터페이스 구현
 * 실제 DB 연동 대신 하드코딩된 더미 데이터 사용
 *
 * @Primary: DB가 없는 환경에서 기본 서비스로 사용
 * ET-01 Employee 테이블 명세에 따른 핵심 필드만 포함:
 * - employeeId, userId, password, name, deptId, deptName, phoneNum
 */
@Service
@Primary  // DB가 없는 환경에서 기본 서비스로 사용
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
                    .deptId("DEPT003")
                    .deptName("교육팀")
                    .phoneNum("010-5678-9012")
                    .build(),
            Employee.builder()
                    .employeeId("EMP006")
                    .userId("choi.horim")
                    .password("password123")
                    .name("최호림")
                    .deptId("DEPT001")
                    .deptName("개발팀")
                    .phoneNum("010-6789-0123")
                    .build(),
            Employee.builder()
                    .employeeId("EMP007")
                    .userId("admin")
                    .password("admin123")
                    .name("관리자")
                    .deptId("DEPT004")
                    .deptName("관리팀")
                    .phoneNum("010-0000-0000")
                    .build()
    );

    /**
     * 모든 직원의 기본 정보를 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getAllBasicInfo() -> List<EmployeeInfoDTO>
     */
    @Override
    public List<EmployeeInfoDTO> getAllBasicInfo() {
        System.out.println("DummyEmployeeService: 전체 직원 기본 정보 조회 요청");

        List<EmployeeInfoDTO> result = DUMMY_EMPLOYEES.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        System.out.println("DummyEmployeeService: 전체 직원 기본 정보 조회 완료: " + result.size() + " 명");
        return result;
    }

    /**
     * 지정된 부서에 속한 직원 목록을 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getEmployeeByDept(depId: String) -> List<EmployeeInfoDTO>
     */
    @Override
    public List<EmployeeInfoDTO> getEmployeeByDept(String deptId) {
        System.out.println("DummyEmployeeService: 부서별 직원 조회 요청: deptId=" + deptId);

        List<EmployeeInfoDTO> result = DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getDeptId().equals(deptId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        System.out.println("DummyEmployeeService: 부서별 직원 조회 완료: deptId=" + deptId + ", 직원 수=" + result.size());
        return result;
    }

    /**
     * 지정된 직원 ID에 해당하는 직원의 상세 정보를 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getEmployeeInfo(employeeId: String) -> Employee
     * 주의: Optional이 아닌 Employee 직접 반환, 없으면 null 반환
     */
    @Override
    public Employee getEmployeeInfo(String employeeId) {
        System.out.println("DummyEmployeeService: 직원 상세 정보 조회 요청: employeeId=" + employeeId);

        Employee result = DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getEmployeeId().equals(employeeId))
                .findFirst()
                .orElse(null);

        if (result != null) {
            System.out.println("DummyEmployeeService: 직원 상세 정보 조회 성공: employeeId=" + employeeId + ", name=" + result.getName());
        } else {
            System.out.println("DummyEmployeeService: 직원 상세 정보 조회 실패: employeeId=" + employeeId);
        }

        return result;
    }

    /**
     * 지정된 이름의 직원 목록을 조회하여 전달하는 비즈니스 로직 메서드
     * 설계 명세: getEmployeeByName(name: String) -> List<EmployeeInfoDTO>
     */
    @Override
    public List<EmployeeInfoDTO> getEmployeeByName(String name) {
        System.out.println("DummyEmployeeService: 이름별 직원 조회 요청: name=" + name);

        List<EmployeeInfoDTO> result = DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getName().contains(name))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        System.out.println("DummyEmployeeService: 이름별 직원 조회 완료: name=" + name + ", 직원 수=" + result.size());
        return result;
    }

    /**
     * 키워드로 직원 검색
     */
    @Override
    public List<EmployeeInfoDTO> searchEmployees(String keyword) {
        System.out.println("DummyEmployeeService: 키워드 검색 요청: keyword=" + keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBasicInfo();
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        List<EmployeeInfoDTO> result = DUMMY_EMPLOYEES.stream()
                .filter(emp ->
                        emp.getName().toLowerCase().contains(lowerKeyword) ||
                                emp.getDeptName().toLowerCase().contains(lowerKeyword) ||
                                emp.getPhoneNum().contains(lowerKeyword)
                )
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        System.out.println("DummyEmployeeService: 키워드 검색 완료: keyword=" + keyword + ", 결과 수=" + result.size());
        return result;
    }

    /**
     * 직원 존재 여부 확인
     */
    @Override
    public boolean existsById(String employeeId) {
        boolean exists = DUMMY_EMPLOYEES.stream()
                .anyMatch(emp -> emp.getEmployeeId().equals(employeeId));

        System.out.println("DummyEmployeeService: 직원 존재 여부 확인: employeeId=" + employeeId + ", exists=" + exists);
        return exists;
    }

    /**
     * 두 직원이 같은 부서인지 확인
     */
    @Override
    public boolean isSameDepartment(String employeeId1, String employeeId2) {
        Employee emp1 = getEmployeeInfo(employeeId1);
        Employee emp2 = getEmployeeInfo(employeeId2);

        boolean sameDept = emp1 != null && emp2 != null &&
                emp1.getDeptId().equals(emp2.getDeptId());

        System.out.println("DummyEmployeeService: 같은 부서 여부 확인: employeeId1=" + employeeId1 +
                ", employeeId2=" + employeeId2 + ", result=" + sameDept);
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
     * 사용자 ID로 직원 조회 (로그인 시 사용)
     */
    public Employee findByUserId(String userId) {
        return DUMMY_EMPLOYEES.stream()
                .filter(emp -> emp.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 부서별 통계 조회
     */
    public List<String> getDepartmentStatistics() {
        return DUMMY_EMPLOYEES.stream()
                .collect(Collectors.groupingBy(Employee::getDeptName, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + "명")
                .collect(Collectors.toList());
    }
}
package com.example.gagso.Employee.controller;

import com.example.gagso.Employee.dto.EmployeeInfoDTO;
import com.example.gagso.Employee.service.DummyEmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Employee 더미 데이터 테스트용 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeTestController {

    private final DummyEmployeeService dummyEmployeeService;

    /**
     * 전체 직원 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<EmployeeInfoDTO>> getAllEmployees() {
        List<EmployeeInfoDTO> employees = dummyEmployeeService.getAllBasicInfo();
        log.info("전체 직원 목록 조회: {} 명", employees.size());
        return ResponseEntity.ok(employees);
    }

    /**
     * 특정 직원 정보 조회
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String employeeId) {
        Optional<Employee> employee = dummyEmployeeService.getEmployeeById(employeeId);

        if (employee.isPresent()) {
            log.info("직원 정보 조회 성공: {}", employeeId);
            return ResponseEntity.ok(employee.get());
        } else {
            log.warn("직원 정보 조회 실패: {}", employeeId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 부서별 직원 목록 조회
     */
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<EmployeeInfoDTO>> getEmployeesByDept(@PathVariable String deptId) {
        List<EmployeeInfoDTO> employees = dummyEmployeeService.getEmployeeByDept(deptId);
        log.info("부서({}) 직원 목록 조회: {} 명", deptId, employees.size());
        return ResponseEntity.ok(employees);
    }

    /**
     * 직원 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeInfoDTO>> searchEmployees(@RequestParam String keyword) {
        List<EmployeeInfoDTO> employees = dummyEmployeeService.searchEmployees(keyword);
        log.info("직원 검색({}): {} 명", keyword, employees.size());
        return ResponseEntity.ok(employees);
    }

    /**
     * 직원 존재 여부 확인
     */
    @GetMapping("/{employeeId}/exists")
    public ResponseEntity<Boolean> checkEmployeeExists(@PathVariable String employeeId) {
        boolean exists = dummyEmployeeService.existsById(employeeId);
        log.info("직원 존재 여부 확인({}): {}", employeeId, exists);
        return ResponseEntity.ok(exists);
    }

    /**
     * 같은 부서 여부 확인
     */
    @GetMapping("/{employeeId1}/same-dept/{employeeId2}")
    public ResponseEntity<Boolean> checkSameDepartment(
            @PathVariable String employeeId1,
            @PathVariable String employeeId2) {
        boolean sameDept = dummyEmployeeService.isSameDepartment(employeeId1, employeeId2);
        log.info("같은 부서 여부 확인({}, {}): {}", employeeId1, employeeId2, sameDept);
        return ResponseEntity.ok(sameDept);
    }

    /**
     * 부서 통계 조회
     */
    @GetMapping("/statistics/departments")
    public ResponseEntity<?> getDepartmentStatistics() {
        var stats = dummyEmployeeService.getAllEmployees().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Employee::getDeptName,
                        java.util.stream.Collectors.counting()
                ));

        log.info("부서별 통계 조회: {}", stats);
        return ResponseEntity.ok(stats);
    }
}
package com.example.gagso.Employee.controller;

// 누락된 Employee 클래스 import 추가
import com.example.gagso.Employee.models.Employee;
import com.example.gagso.Employee.service.EmployeeInfoProvider;
import com.example.gagso.Employee.dto.EmployeeInfoDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 직원 관리 테스트용 컨트롤러
 * 설계 기술서의 DCD1008 - EmployeeController 기반
 */
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeTestController {

    @Autowired
    private EmployeeInfoProvider employeeInfoProvider;

    /**
     * 전체 직원 목록 조회
     * GET /api/employees
     */
    @GetMapping
    public ResponseEntity<List<EmployeeInfoDTO>> getAllEmployees() {
        try {
            List<EmployeeInfoDTO> employees = employeeInfoProvider.getAllBasicInfo();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 직원 정보 조회
     * GET /api/employees/{employeeId}
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String employeeId) {
        try {
            Employee employee = employeeInfoProvider.getEmployeeInfo(employeeId);
            if (employee != null) {
                return ResponseEntity.ok(employee);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 부서별 직원 조회
     * GET /api/employees/department/{deptId}
     */
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<EmployeeInfoDTO>> getEmployeesByDepartment(@PathVariable String deptId) {
        try {
            List<EmployeeInfoDTO> employees = employeeInfoProvider.getEmployeeByDept(deptId);
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 이름으로 직원 검색
     * GET /api/employees/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeInfoDTO>> searchEmployeesByName(@RequestParam String name) {
        try {
            List<EmployeeInfoDTO> employees = employeeInfoProvider.getEmployeeByName(name);
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 헬스체크 엔드포인트
     * GET /api/employees/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Employee service is running");
    }
}
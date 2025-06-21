package com.example.gagso.Employees.controller;

import com.example.gagso.Employees.dto.EmployeeRegisterRequestDTO;
import com.example.gagso.Employees.dto.EmployeeRegistrationResult;
import com.example.gagso.Employees.models.Employee;
import com.example.gagso.Employees.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 직원 등록, 조회를 담당하는 컨트롤 클래스
 * 설계 명세: ECD3005
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 직원 정보를 전달받아 직원 등록을 요청
     * 설계 명세: registerEmployee
     */
    @PostMapping
    public ResponseEntity<?> registerEmployee(@RequestBody EmployeeRegisterRequestDTO employeeDto) {
        log.info("직원 등록 요청: 직원 ID {}, 이름 '{}'", employeeDto.getEmployeeId(), employeeDto.getName());

        try {
            EmployeeRegistrationResult result = employeeService.register(employeeDto);

            if (result.isSuccess()) {
                log.info("직원 등록 성공: 직원 ID {}", result.getEmployee().getEmployeeId());
                return ResponseEntity.ok(result);
            } else {
                log.warn("직원 등록 실패: {}", result.getErrors());
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("직원 등록 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("직원 등록 중 시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 전체 직원 조회 화면을 출력
     * 설계 명세: displayEmployee
     */
    @GetMapping
    public ResponseEntity<List<Employee>> displayEmployee() {
        log.info("전체 직원 조회 요청");

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            log.info("전체 직원 조회 완료: {} 명", employees.size());
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            log.error("전체 직원 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 이름으로 직원 조회
     * 설계 명세: displayEmployeeByName
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<List<Employee>> displayEmployeeByName(@PathVariable String name) {
        log.info("이름별 직원 조회 요청: 이름 '{}'", name);

        try {
            List<Employee> employees = employeeService.getEmployeeByName(name);
            log.info("이름별 직원 조회 완료: {} 명", employees.size());
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            log.error("이름별 직원 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 부서 ID로 직원 조회
     * 설계 명세: displayEmployeeByDepId
     */
    @GetMapping("/department/{depId}")
    public ResponseEntity<List<Employee>> displayEmployeeByDepId(@PathVariable String depId) {
        log.info("부서별 직원 조회 요청: 부서 ID '{}'", depId);

        try {
            List<Employee> employees = employeeService.getEmployeeByDepId(depId);
            log.info("부서별 직원 조회 완료: {} 명", employees.size());
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            log.error("부서별 직원 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 직원 ID로 개별 직원 조회
     * 설계 명세: displayEmployeeById
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> displayEmployeeById(@PathVariable String employeeId) {
        log.info("개별 직원 조회 요청: 직원 ID '{}'", employeeId);

        try {
            Employee employee = employeeService.getEmployeeById(employeeId);
            log.info("개별 직원 조회 완료: {}", employee.getName());
            return ResponseEntity.ok(employee);

        } catch (IllegalArgumentException e) {
            log.warn("직원을 찾을 수 없음: {}", employeeId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("개별 직원 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 부서명으로 직원 조회
     */
    @GetMapping("/department/name/{depName}")
    public ResponseEntity<List<Employee>> displayEmployeeByDepName(@PathVariable String depName) {
        log.info("부서명별 직원 조회 요청: 부서명 '{}'", depName);

        try {
            List<Employee> employees = employeeService.getEmployeesByDepName(depName);
            log.info("부서명별 직원 조회 완료: {} 명", employees.size());
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            log.error("부서명별 직원 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 전화번호로 직원 조회
     */
    @GetMapping("/phone/{phoneNum}")
    public ResponseEntity<Employee> displayEmployeeByPhoneNum(@PathVariable String phoneNum) {
        log.info("전화번호별 직원 조회 요청: 전화번호 '{}'", phoneNum);

        try {
            Employee employee = employeeService.getEmployeeByPhoneNum(phoneNum);
            log.info("전화번호별 직원 조회 완료: {}", employee.getName());
            return ResponseEntity.ok(employee);

        } catch (IllegalArgumentException e) {
            log.warn("해당 전화번호의 직원을 찾을 수 없음: {}", phoneNum);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("전화번호별 직원 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 직원 정보 삭제
     */
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        log.info("직원 삭제 요청: 직원 ID {}", employeeId);

        try {
            employeeService.deleteEmployee(employeeId);
            log.info("직원 삭제 완료: {}", employeeId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("삭제할 직원을 찾을 수 없음: {}", employeeId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("직원 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 직원 ID 중복 확인
     */
    @GetMapping("/check/id/{employeeId}")
    public ResponseEntity<Boolean> checkEmployeeIdExists(@PathVariable String employeeId) {
        log.info("직원 ID 중복 확인 요청: {}", employeeId);

        try {
            boolean exists = employeeService.existsByEmployeeId(employeeId);
            log.info("직원 ID 중복 확인 완료: {}", exists);
            return ResponseEntity.ok(exists);

        } catch (Exception e) {
            log.error("직원 ID 중복 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 전화번호 중복 확인
     */
    @GetMapping("/check/phone/{phoneNum}")
    public ResponseEntity<Boolean> checkPhoneNumExists(@PathVariable String phoneNum) {
        log.info("전화번호 중복 확인 요청: {}", phoneNum);

        try {
            boolean exists = employeeService.existsByPhoneNum(phoneNum);
            log.info("전화번호 중복 확인 완료: {}", exists);
            return ResponseEntity.ok(exists);

        } catch (Exception e) {
            log.error("전화번호 중복 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

package com.example.gagso.Employees.controller;

import com.example.gagso.Department.dto.DeptInfoDTO;
import com.example.gagso.Department.service.DepartmentInfoProvider;
import com.example.gagso.Employees.dto.EmployeeInfoDTO; // ⭐ EmployeeInfoDTO 임포트
import com.example.gagso.Employees.dto.EmployeeRegisterRequestDTO;
import com.example.gagso.Employees.dto.EmployeeRegistrationResult;
// import com.example.gagso.Employees.models.Employee; // ⭐ Employee 엔티티는 직접 반환하지 않으므로 제거 가능 (또는 필요에 따라 유지)
import com.example.gagso.Employees.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors; // ⭐ stream 사용을 위한 임포트

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
    private final DepartmentInfoProvider departmentInfoProvider;

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
    public ResponseEntity<List<EmployeeInfoDTO>> displayEmployee() { // ⭐ 반환 타입 변경
        log.info("전체 직원 조회 요청");

        try {
            List<EmployeeInfoDTO> employees = employeeService.getAllEmployeesInfo(); // ⭐ DTO를 반환하는 서비스 메서드 호출
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
    public ResponseEntity<List<EmployeeInfoDTO>> displayEmployeeByName(@PathVariable String name) { // ⭐ 반환 타입 변경
        log.info("이름별 직원 조회 요청: 이름 '{}'", name);

        try {
            List<EmployeeInfoDTO> employees = employeeService.getEmployeeInfoByName(name); // ⭐ DTO를 반환하는 서비스 메서드 호출
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
    public ResponseEntity<List<EmployeeInfoDTO>> displayEmployeeByDepId(@PathVariable String depId) { // ⭐ 반환 타입 변경
        log.info("부서별 직원 조회 요청: 부서 ID '{}'", depId);

        try {
            List<EmployeeInfoDTO> employees = employeeService.getEmployeeInfoByDepId(depId); // ⭐ DTO를 반환하는 서비스 메서드 호출
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
    public ResponseEntity<EmployeeInfoDTO> displayEmployeeById(@PathVariable String employeeId) { // ⭐ 반환 타입 변경
        log.info("개별 직원 조회 요청: 직원 ID '{}'", employeeId);

        try {
            EmployeeInfoDTO employee = employeeService.getEmployeeInfoById(employeeId); // ⭐ DTO를 반환하는 서비스 메서드 호출
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
    public ResponseEntity<List<EmployeeInfoDTO>> displayEmployeeByDepName(@PathVariable String depName) { // ⭐ 반환 타입 변경
        log.info("부서명별 직원 조회 요청: 부서명 '{}'", depName);

        try {
            List<EmployeeInfoDTO> employees = employeeService.getEmployeesInfoByDepName(depName); // ⭐ DTO를 반환하는 서비스 메서드 호출
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
    public ResponseEntity<EmployeeInfoDTO> displayEmployeeByPhoneNum(@PathVariable String phoneNum) { // ⭐ 반환 타입 변경
        log.info("전화번호별 직원 조회 요청: 전화번호 '{}'", phoneNum);

        try {
            EmployeeInfoDTO employee = employeeService.getEmployeeInfoByPhoneNum(phoneNum); // ⭐ DTO를 반환하는 서비스 메서드 호출
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

    @GetMapping("/departments")
    public ResponseEntity<List<DeptInfoDTO>> getDepartmentsList() {
        log.info("부서 목록 조회 요청 (EmployeeController를 통해)");
        try {
            List<DeptInfoDTO> departments = departmentInfoProvider.getDeptInfo();
            log.info("부서 목록 조회 완료: {} 개", departments.size());
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            log.error("부서 목록 조회 중 오류 발생 (EmployeeController)", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
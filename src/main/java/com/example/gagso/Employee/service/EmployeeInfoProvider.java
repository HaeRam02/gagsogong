package com.example.gagso.Employee.service;

import com.example.gagso.Employee.dto.EmployeeInfoDTO;
import com.example.gagso.Employee.models.Employee;

import java.util.List;

/**
 * 직원 정보를 조회하거나 제공하는 기능의 계약을 정의하는 비즈니스 인터페이스
 * 설계 명세: DCD1005 - interface EmployeeInfoProvider
 * 지속성: Transient
 */
public interface EmployeeInfoProvider {

    /**
     * 모든 직원의 기본 정보를 조회하기 위한 기능을 정의한 메서드 인터페이스
     * 설계 명세: getAllBasicInfo() -> List<EmployeeInfoDTO>
     */
    List<EmployeeInfoDTO> getAllBasicInfo();

    /**
     * 지정된 부서에 속한 직원 정보를 조회하기 위한 기능을 정의한 메서드 인터페이스
     * 설계 명세: getEmployeeByDept(depId: String) -> List<EmployeeInfoDTO>
     */
    List<EmployeeInfoDTO> getEmployeeByDept(String deptId);

    /**
     * 지정된 직원 ID에 해당하는 직원의 상세 정보를 조회하기 위한 기능을 정의한 메서드 인터페이스
     * 설계 명세: getEmployeeInfo(employeeId: String) -> Employee
     * 주의: Optional이 아닌 Employee 직접 반환, 없으면 null
     */
    Employee getEmployeeInfo(String employeeId);

    /**
     * 직원 이름을 기준으로 해당 직원 정보를 조회하기 위한 기능을 정의한 메서드 인터페이스
     * 설계 명세: getEmployeeByName(name: String) -> List<EmployeeInfoDTO>
     */
    List<EmployeeInfoDTO> getEmployeeByName(String name);

    /**
     * 키워드로 직원 검색
     */
    List<EmployeeInfoDTO> searchEmployees(String keyword);

    /**
     * 직원 존재 여부 확인
     */
    boolean existsById(String employeeId);

    /**
     * 두 직원이 같은 부서인지 확인
     */
    boolean isSameDepartment(String employeeId1, String employeeId2);
}
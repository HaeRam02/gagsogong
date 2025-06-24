package com.example.gagso.Employees.service;

import com.example.gagso.Employees.dto.EmployeeInfoDTO;
import java.util.List;

/**
 * 다른 서브시스템에서 직원 정보 조회를 요청할 때 사용하는 인터페이스
 * 직원 정보 제공 서비스의 표준 인터페이스
 */
public interface EmployeeInfoProvider {

    /**
     * 모든 직원의 기본 정보를 조회합니다.
     * @return 모든 직원의 기본 정보 리스트
     */
    List<EmployeeInfoDTO> getAllBasicInfo();

    /**
     * 특정 부서의 직원 정보를 조회합니다.
     * @param deptId 부서 ID
     * @return 해당 부서 직원 정보 리스트
     */
    List<EmployeeInfoDTO> getEmployeeByDept(String deptId);

    /**
     * 특정 직원의 상세 정보를 조회합니다.
     * @param employeeId 직원 ID
     * @return 직원 상세 정보 (없으면 null)
     */
    EmployeeInfoDTO getEmployeeInfo(String employeeId);

    /**
     * 직원 이름으로 직원 정보를 조회합니다.
     * @param name 직원 이름
     * @return 해당 이름의 직원 정보 리스트
     */
    List<EmployeeInfoDTO> getEmployeeByName(String name);
}
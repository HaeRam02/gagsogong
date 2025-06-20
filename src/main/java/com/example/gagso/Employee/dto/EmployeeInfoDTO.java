package com.example.gagso.Employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 계층 간 데이터 전달을 위해 사용되는 비영속성 직원 정보 객체
 * 설계 명세: DCD1004 - DTO EmployeeInfoDTO
 * 지속성: Transient
 *
 * ET-01 Employee 테이블 명세에 따른 핵심 필드만 포함:
 * - employeeId, name, deptId, deptName, phoneNum
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeInfoDTO {

    /**
     * 직원의 ID 정보를 담는 데이터 전달 필드
     */
    private String employeeId;

    /**
     * 이름 정보를 담는 데이터 전달 필드
     */
    private String name;

    /**
     * 부서 ID 정보를 담는 데이터 전달 필드
     */
    private String deptId;

    /**
     * 부서 이름 정보를 담는 데이터 전달 필드
     */
    private String deptName;

    /**
     * 전화번호 정보를 담는 데이터 전달 필드
     */
    private String phoneNumber;

    /**
     * 화면 표시용 문자열 (이름 + 부서명)
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", name, deptName);
    }

    /**
     * 검색 결과 표시용 포맷
     */
    public String getDisplayName() {
        return String.format("%s - %s", name, deptName);
    }

    /**
     * 간단한 정보 표시용
     */
    public String getSimpleInfo() {
        return String.format("%s (%s)", name, deptName);
    }

    /**
     * 연락처 정보 포함 표시용
     */
    public String getFullInfo() {
        return String.format("%s (%s) [%s]", name, deptName, phoneNumber);
    }
}
// Employee.java - Spring Boot 3.x 호환 버전 (jakarta.persistence 사용)
package com.example.gagso.Employee.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * 직원 정보를 담는 엔티티 클래스
 * 설계 명세: DCD1001 - Entity Employee
 * 테이블 명세: ET-01 Employee
 * 지속성: Persistent
 *
 * Spring Boot 3.x 호환 (jakarta.persistence 사용)
 * 데이터베이스 테이블 명세 ET-01에 따른 핵심 필드만 구현:
 * - employeeId: 고유 ID (PK) - VARCHAR(36)
 * - userId: 로그인 ID - VARCHAR(20)
 * - password: 비밀번호 - VARCHAR(20)
 * - name: 이름 - VARCHAR(20)
 * - deptId: 부서 ID (FK to Department) - VARCHAR(36)
 * - deptName: 부서 이름 - VARCHAR(20)
 * - phoneNum: 전화번호 - VARCHAR(20)
 */
@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    /**
     * 직원 고유 식별자 (Primary Key)
     * UUID 형태의 36자리 문자열
     */
    @Id
    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;

    /**
     * 로그인용 사용자 ID
     * 시스템 로그인 시 사용되는 고유 아이디
     */
    @Column(name = "user_id", length = 20, nullable = false, unique = true)
    private String userId;

    /**
     * 로그인용 비밀번호
     * 설계 명세상 VARCHAR(20)이지만 실제로는 암호화되어 저장
     */
    @Column(name = "password", length = 20, nullable = false)
    private String password;

    /**
     * 직원 실명
     */
    @Column(name = "name", length = 20, nullable = false)
    private String name;

    /**
     * 부서 ID (외래키)
     * Department 테이블의 deptId를 참조
     * 더미 데이터 사용을 위해 제약조건 제거
     */
    @Column(name = "dept_id", length = 36, nullable = false)
    private String deptId;

    /**
     * 부서명 (비정규화)
     * 조회 성능을 위해 부서명을 직접 저장
     */
    @Column(name = "dept_name", length = 20, nullable = false)
    private String deptName;

    /**
     * 전화번호
     */
    @Column(name = "phone_num", length = 20, nullable = false)
    private String phoneNum;

    /**
     * 비즈니스 메서드: 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 비즈니스 메서드: 부서 이동
     */
    public void transferDepartment(String newDeptId, String newDeptName) {
        this.deptId = newDeptId;
        this.deptName = newDeptName;
    }

    /**
     * 비즈니스 메서드: 같은 부서 직원인지 확인
     */
    public boolean isSameDepartment(Employee other) {
        return this.deptId != null && this.deptId.equals(other.getDeptId());
    }

    /**
     * toString 메서드 (디버깅용, 비밀번호 제외)
     */
    @Override
    public String toString() {
        return String.format("Employee{id='%s', userId='%s', name='%s', dept='%s'}",
                employeeId, userId, name, deptName);
    }

    /**
     * equals & hashCode (employeeId 기준)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee employee = (Employee) obj;
        return employeeId != null && employeeId.equals(employee.employeeId);
    }

    @Override
    public int hashCode() {
        return employeeId != null ? employeeId.hashCode() : 0;
    }
}
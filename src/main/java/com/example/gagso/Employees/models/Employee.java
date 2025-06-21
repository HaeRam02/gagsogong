package com.example.gagso.Employees.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * 직원 정보를 담는 엔티티 클래스
 * 설계 명세: ECD2001
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    /**
     * 직원 ID (기본키)
     */
    @Id
    @Column(name = "employee_id", length = 20, nullable = false)
    private String employeeId;

    /**
     * 직원 비밀번호
     */
    @Column(name = "password", length = 20, nullable = false)
    private String password;

    /**
     * 직원 이름
     */
    @Column(name = "name", length = 20, nullable = false)
    private String name;

    /**
     * 부서 ID
     */
    @Column(name = "dep_id", length = 20, nullable = false)
    private String depId;

    /**
     * 부서 이름
     */
    @Column(name = "dep_name", length = 20, nullable = false)
    private String depName;

    /**
     * 전화번호
     */
    @Column(name = "phone_num", length = 13, nullable = false, unique = true)
    private String phoneNum;

    /**
     * 디버깅용 toString (비밀번호 제외)
     */
    @Override
    public String toString() {
        return String.format("Employee{employeeId='%s', name='%s', depId='%s', depName='%s', phoneNum='%s'}",
                employeeId, name, depId, depName, phoneNum);
    }
}
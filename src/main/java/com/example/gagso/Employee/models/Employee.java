package com.example.gagso.Employee.models;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

/**
 * 직원 정보를 담는 엔티티
 * 일정의 GROUP 공개범위에서 부서 확인용으로 사용
 */
@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "dept_id", length = 50)
    private String deptId;

    @Column(name = "dept_name", length = 100)
    private String deptName;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // 기타 필요한 필드들...
}
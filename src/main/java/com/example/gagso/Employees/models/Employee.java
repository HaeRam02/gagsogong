package com.example.gagso.Employees.models;

import com.example.gagso.Department.models.Department; // Department 엔티티를 임포트
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * 직원 정보를 담는 엔티티 클래스
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
     * 부서 정보 (외래키 관계 매핑)
     * 직원은 하나의 부서에 속하므로 ManyToOne 관계
     */
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 (필요할 때만 Department 정보를 가져옴)
    @JoinColumn(name = "dept_id", nullable = false) // employees 테이블의 dept_id 컬럼이 Department 테이블의 deptId를 참조
    private Department department; // Department 엔티티 참조

    /**
     * 전화번호
     */
    @Column(name = "phone_num", length = 13, nullable = false, unique = true)
    private String phoneNum;

    /**
     * 디버깅용 toString (비밀번호 제외, department 이름 포함)
     */
    @Override
    public String toString() {
        // department가 null이 아닐 경우에만 department.getDeptTitle() 호출
        String departmentName = (department != null) ? department.getDeptTitle() : "N/A";
        return String.format("Employee{employeeId='%s', name='%s', deptId='%s', deptName='%s', phoneNum='%s'}",
                employeeId, name,
                (department != null ? department.getDeptId() : "N/A"), // 부서 ID
                departmentName, // 부서 이름
                phoneNum);
    }
}
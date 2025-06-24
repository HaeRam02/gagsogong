package com.example.gagso.Employees.repository;

import com.example.gagso.Employees.models.Employee;
import com.example.gagso.Department.models.Department; // Department 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    // 기본 CRUD는 JpaRepository에서 제공

    Optional<Employee> findByEmployeeId(String employeeId);

    List<Employee> findByName(String name);

    // ⭐ 부서 ID로 직원 조회 (관계 매핑 활용)
    List<Employee> findByDepartment_DeptId(String deptId);

    // ⭐ 부서명으로 직원 조회 (관계 매핑 활용)
    List<Employee> findByDepartment_DeptTitle(String deptTitle);

    Optional<Employee> findByPhoneNum(String phoneNum);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByPhoneNum(String phoneNum);

    // EmployeeRepository.register() 대신 save()를 사용하므로 해당 메서드가 없으면 추가할 필요 없음.
    // 만약 register()라는 커스텀 메서드가 필요하다면, 직접 구현하거나 JpaRepository의 save()를 활용.

    // 전체 직원 조회 정렬 (필요하다면)
    // List<Employee> findAllByOrderByDepartment_DeptTitleAscNameAsc(); // 예시: 부서명, 이름 순 정렬
}
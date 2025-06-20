package com.example.gagso.Employee.repository;

import com.example.gagso.Employee.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    /**
     * 직원 ID로 직원 정보 조회
     */
    Optional<Employee> findByEmployeeId(String employeeId);

    /**
     * 부서 ID로 해당 부서 직원들 조회
     */
    List<Employee> findByDeptId(String deptId);

    /**
     * 두 직원이 같은 부서인지 확인
     */
    @Query("""
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END 
        FROM Employee e1, Employee e2 
        WHERE e1.employeeId = :employeeId1 
          AND e2.employeeId = :employeeId2 
          AND e1.deptId = e2.deptId
          AND e1.deptId IS NOT NULL
    """)
    boolean isSameDepartment(@Param("employeeId1") String employeeId1,
                             @Param("employeeId2") String employeeId2);

    /**
     * 직원명으로 검색
     */
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:keyword% ORDER BY e.name")
    List<Employee> findByNameContaining(@Param("keyword") String keyword);

    /**
     * 부서명으로 검색
     */
    @Query("SELECT e FROM Employee e WHERE e.deptName LIKE %:keyword% ORDER BY e.deptName, e.name")
    List<Employee> findByDeptNameContaining(@Param("keyword") String keyword);
}
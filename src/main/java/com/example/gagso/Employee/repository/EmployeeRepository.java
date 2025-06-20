package com.example.gagso.Employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 데이터베이스에서 직원 정보를 조회, 저장하는 기능을 담당하는 데이터 접근 객체
 * 설계 명세: DCD1002 - DAO EmployeeRepository
 * 지속성: Persistence
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    /**
     * 전체 직원 정보 검색
     * 설계 명세: findAll() -> List<Employee>
     */
    @Override
    List<Employee> findAll();

    /**
     * 직원 ID에 맞는 직원 정보 검색
     * 설계 명세: findOne(id: String) -> Employee
     */
    @Query("SELECT e FROM Employee e WHERE e.employeeId = :id")
    Optional<Employee> findOne(@Param("id") String employeeId);

    /**
     * 직원 ID로 직원 정보 조회 (표준 메서드)
     */
    Optional<Employee> findByEmployeeId(String employeeId);

    /**
     * 사용자 ID로 직원 정보 조회 (로그인 시 사용)
     */
    Optional<Employee> findByUserId(String userId);

    /**
     * 부서에 소속되어 있는 직원 정보 검색
     * 설계 명세: findEmployeeByDepId(depId: String) -> List<Employee>
     */
    @Query("SELECT e FROM Employee e WHERE e.deptId = :deptId ORDER BY e.name")
    List<Employee> findEmployeeByDepId(@Param("deptId") String deptId);

    /**
     * 부서명으로 소속 직원 정보 검색
     * 설계 명세: findEmployeeByDepName(depName: String) -> List<Employee>
     */
    @Query("SELECT e FROM Employee e WHERE e.deptName LIKE %:deptName% ORDER BY e.deptName, e.name")
    List<Employee> findEmployeeByDepName(@Param("deptName") String deptName);

    /**
     * 이름에 맞는 직원 정보 검색
     * 설계 명세: findEmployeeByName(name: String) -> List<Employee>
     */
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:name% ORDER BY e.name")
    List<Employee> findEmployeeByName(@Param("name") String name);

    /**
     * 직원 정보 저장
     * 설계 명세: save(employee: Employee) -> Employee
     */
    @Override
    <S extends Employee> S save(S employee);

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
     * 키워드 통합 검색 (이름, 부서명, 사용자ID)
     */
    @Query("""
        SELECT e FROM Employee e 
        WHERE (e.name LIKE %:keyword% 
            OR e.deptName LIKE %:keyword% 
            OR e.userId LIKE %:keyword%)
        ORDER BY e.name
    """)
    List<Employee> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 부서별 직원 수 조회
     */
    @Query("SELECT e.deptName, COUNT(e) FROM Employee e GROUP BY e.deptName, e.deptId")
    List<Object[]> countEmployeesByDepartment();
}
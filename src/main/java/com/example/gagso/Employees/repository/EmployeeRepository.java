package com.example.gagso.Employees.repository;

import com.example.gagso.Employees.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 직원 객체의 저장 및 조회를 저장소와 연결된 형태로 수행하는 데이터 접근 객체
 * 설계 명세: ECD3015
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    /**
     * 전체 직원 조회 (부서명, 이름 순으로 정렬)
     */
    @Query("SELECT e FROM Employee e ORDER BY e.depName ASC, e.name ASC")
    List<Employee> findAllByOrderByDepNameAscNameAsc();

    /**
     * 특정 직원 ID를 받아 해당 직원 정보를 반환
     */
    Optional<Employee> findByEmployeeId(String employeeId);

    /**
     * 이름으로 직원 조회
     */
    List<Employee> findByName(String name);

    /**
     * 부서 ID로 직원 조회
     */
    List<Employee> findByDepId(String depId);

    /**
     * 부서 이름으로 직원 조회
     */
    List<Employee> findByDepName(String depName);

    /**
     * 전화번호로 직원 조회
     */
    Optional<Employee> findByPhoneNum(String phoneNum);

    /**
     * 직원 ID 존재 여부 확인
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * 전화번호 존재 여부 확인
     */
    boolean existsByPhoneNum(String phoneNum);

    /**
     * 직원 등록 (save와 동일하지만 명시적으로 구분)
     */
    default Employee register(Employee employee) {
        return save(employee);
    }
}
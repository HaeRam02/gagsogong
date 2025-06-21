// src/main/java/com/example/gagso/Employee/initializer/EmployeeDataInitializer.java
package com.example.gagso.Employee.initializer;

import com.example.gagso.Employee.models.Employee;
import com.example.gagso.Employee.repository.EmployeeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class EmployeeDataInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;

    public EmployeeDataInitializer(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 테이블이 비어 있을 때만 초기 데이터 삽입
        if (employeeRepository.count() == 0) {
            List<Employee> initialEmployees = List.of(
                    Employee.builder()
                            .employeeId("EMP001")
                            .userId("kim.donghyun")
                            .password("password123")
                            .name("김동현")
                            .deptId("DEPT001")
                            .deptName("개발팀")
                            .phoneNum("010-1234-5678")
                            .build(),
                    Employee.builder()
                            .employeeId("EMP002")
                            .userId("kang.yunjae")
                            .password("password123")
                            .name("강윤제")
                            .deptId("DEPT001")
                            .deptName("개발팀")
                            .phoneNum("010-2345-6789")
                            .build(),
                    Employee.builder()
                            .employeeId("EMP003")
                            .userId("koo.gayeon")
                            .password("password123")
                            .name("구가연")
                            .deptId("DEPT001")
                            .deptName("개발팀")
                            .phoneNum("010-3456-7890")
                            .build(),
                    Employee.builder()
                            .employeeId("EMP004")
                            .userId("sung.hyeram")
                            .password("password123")
                            .name("성혜람")
                            .deptId("DEPT002")
                            .deptName("기획팀")
                            .phoneNum("010-4567-8901")
                            .build(),
                    Employee.builder()
                            .employeeId("EMP005")
                            .userId("jang.seyeon")
                            .password("password123")
                            .name("장세연")
                            .deptId("DEPT003")
                            .deptName("교육팀")
                            .phoneNum("010-5678-9012")
                            .build(),
                    Employee.builder()
                            .employeeId("EMP006")
                            .userId("choi.horim")
                            .password("password123")
                            .name("최호림")
                            .deptId("DEPT001")
                            .deptName("개발팀")
                            .phoneNum("010-6789-0123")
                            .build(),
                    Employee.builder()
                            .employeeId("TEMP_USER_001")
                            .userId("temp.user")
                            .password("tempPass123")
                            .name("홍길동")
                            .deptId("DEPT_001")
                            .deptName("임시부서")
                            .phoneNum("010-1111-2222")
                            .build()
            );

            // JPA 배치 INSERT
            employeeRepository.saveAll(initialEmployees);
            System.out.println("초기 직원 데이터 삽입 완료: " + initialEmployees.size() + "명");
        }
    }
}

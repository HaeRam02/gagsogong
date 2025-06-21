package com.example.gagso.Department.repository;

import com.example.gagso.Department.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, String> {
}
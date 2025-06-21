package com.example.gagso.Department.controller;

import com.example.gagso.Department.dto.DeptInfoDTO;
import com.example.gagso.Department.service.DepartmentInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DeptController {

    private final DepartmentInfoProvider departmentProvider;

    @GetMapping
    public List<DeptInfoDTO> getDepartments() {
        return departmentProvider.getDeptInfo();
    }
}
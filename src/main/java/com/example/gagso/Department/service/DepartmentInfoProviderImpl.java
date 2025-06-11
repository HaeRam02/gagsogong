package com.example.gagso.Department.service;

import com.example.gagso.Department.dto.DeptInfoDTO;
import com.example.gagso.Department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentInfoProviderImpl implements DepartmentInfoProvider {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<DeptInfoDTO> getDeptInfo() {
        return departmentRepository.findAll().stream()
                .map(department -> new DeptInfoDTO(department.getDeptId(), department.getDeptTitle()))
                .collect(Collectors.toList());
    }
}
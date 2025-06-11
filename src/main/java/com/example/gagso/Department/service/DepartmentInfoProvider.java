package com.example.gagso.Department.service;

import com.example.gagso.Department.dto.DeptInfoDTO;
import java.util.List;

public interface DepartmentInfoProvider {
    List<DeptInfoDTO> getDeptInfo();
}
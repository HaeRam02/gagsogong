package com.example.gagso.Department.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Department {

    @Id
    private String deptId;

    @Column(name = "deptTitle", length = 100, nullable = false)
    private String deptTitle;
}
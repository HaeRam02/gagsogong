package com.example.gagso.Educations.repository;

import com.example.gagso.Educations.models.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, String> {
}

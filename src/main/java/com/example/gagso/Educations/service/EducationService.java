package com.example.gagso.Educations.service;

import com.example.gagso.Educations.models.Education;
import com.example.gagso.Educations.repository.EducationRepository;
import com.example.gagso.Educations.dto.EducationDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EducationService {

    private final EducationRepository repository;

    public EducationService(EducationRepository repository) {
        this.repository = repository;
    }

    public List<Education> findAll() {
        return repository.findAll();
    }

    public Optional<Education> findById(String id) {
        return repository.findById(id);
    }

    public Education save(Education education) {
        return repository.save(education);
    }

    public void create(EducationDto dto) {
        Education edu = new Education();
        edu.setEducationId(UUID.randomUUID().toString());
        edu.setTitle(dto.getTitle());
        edu.setInstructor(dto.getInstructor());
        edu.setEducationType(dto.getEducation_type());
        edu.setApplicationPeriodStart(dto.getApplication_period_start());
        edu.setApplicationPeriodEnd(dto.getApplication_period_end());
        edu.setEducationPeriodStart(dto.getEducation_period_start());
        edu.setEducationPeriodEnd(dto.getEducation_period_end());
        edu.setAttachmentType(dto.getAttachment_type());
        edu.setAttachmentPath(dto.getAttachment_path());

        repository.save(edu); // ✅ 여기 수정됨
    }
}

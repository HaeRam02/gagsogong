package com.example.gagso.Educations.controller;

import com.example.gagso.Educations.models.Education;
import com.example.gagso.Educations.service.EducationService;
import com.example.gagso.Educations.dto.EducationDto;
import com.example.gagso.Log.service.EducationLogWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.MediaType;
import com.example.gagso.Log.model.ActionType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/educations")
@CrossOrigin(origins = "*")
public class EducationController {

    private final EducationService service;
    private final EducationLogWriter educationLogWriter; // EducationLogWriter 필드 추가

    @Autowired
    public EducationController(EducationService service, EducationLogWriter educationLogWriter) {
        this.service = service;
        this.educationLogWriter = educationLogWriter;

    }

    @GetMapping
    public List<Education> getAllEducations() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Education getEducationById(@PathVariable("id") String id) {
        return service.findById(id)
                .orElseThrow(() -> new RuntimeException("교육 정보가 존재하지 않습니다."));
    }

    @PostMapping
    public ResponseEntity<Education> uploadEducation(
            @RequestPart("education") EducationDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            String savedFileName = null;

            if (file != null && !file.isEmpty()) {
                String originalName = file.getOriginalFilename();

                // 🔐 확장자 체크 및 이름 sanitize(필요 시)
                String extension = "";
                int i = originalName.lastIndexOf('.');
                if (i > 0) extension = originalName.substring(i);
                String baseName = UUID.randomUUID().toString(); // 중복 방지
                String safeFileName = baseName + extension;

                // 🔽 uploads/ 폴더 지정 (루트 기준 상대 경로)
                Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();

                Files.createDirectories(uploadPath);

                Path targetPath = uploadPath.resolve(safeFileName);
                file.transferTo(targetPath);

                savedFileName = safeFileName; // UUID 이름 저장 (또는 originalName도 함께 저장해도 됨)
            }

            if (savedFileName != null) {
                dto.setAttachment_path(savedFileName);  // ✅ DB에 저장되는 파일명
            }

            Education createdEducation = service.create(dto);
            String actorId = "ADMIN_001";
            educationLogWriter.save(actorId, ActionType.REGISTER, createdEducation);
            return ResponseEntity.ok(createdEducation); // ⭐ 반환 구문 변경
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }




    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename) {    try {
            Path filePath = Paths.get("uploads").toAbsolutePath().resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}

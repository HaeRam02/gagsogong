package com.example.gagso.Documents.controller;

import com.example.gagso.Documents.dto.DocumentDTO;
import com.example.gagso.Documents.service.DocumentService;
import com.example.gagso.WorkRoom.dto.TaskDTO;
import com.example.gagso.WorkRoom.dto.TaskListItemDTO;
import com.example.gagso.WorkRoom.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService service;

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> registerDocument(
            @RequestPart("documentDTO") DocumentDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String msg = service.register(dto, file);
        if (!msg.isEmpty()) {
            System.out.println("try doc register");
            return ResponseEntity.badRequest().body(msg);
        }
        return ResponseEntity.ok("업무가 등록되었습니다.");
    }

    @GetMapping
    public List<DocumentDTO> loadAllDocuments() {
        return service.getAllDocuments();
    }
//
//    @GetMapping("/search")
//    public List<DocumentDTO> searchDocuments(@RequestParam("title") String title) {
//        return service.search(title);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable("id") String id) {
        DocumentDTO dto = service.getDocumentById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
//    @GetMapping("/open")
//    public ResponseEntity<?> openCreateScreen(@RequestParam String deptId) {
//        return ResponseEntity.ok("부서 ID 확인 완료: " + deptId);
//    }
}
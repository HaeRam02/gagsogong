package com.example.gagso.Documents.controller;

import com.example.gagso.Documents.dto.DocumentDTO;
import com.example.gagso.Documents.service.DocumentService;
import com.example.gagso.WorkRoom.dto.TaskDTO;
import com.example.gagso.WorkRoom.dto.TaskListItemDTO;
import com.example.gagso.WorkRoom.service.TaskService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
            @RequestPart(value = "files", required = false)  List<MultipartFile>  files) {

        String msg = service.register(dto, files);
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
    @GetMapping("/{docId}/attachments/{attId}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String docId,
            @PathVariable String attId) {

        // Service 레이어에서 Resource 형태로 파일을 로드
        Resource file = service.loadAttachmentAsResource(docId, attId);

        // 원본 파일명 추출 (서비스에서 함께 반환하거나, DB에서 조회)
        String originalFilename = service.getAttachmentFilename(attId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + originalFilename + "\"")
                .body(file);
    }
//    @GetMapping("/open")
//    public ResponseEntity<?> openCreateScreen(@RequestParam String deptId) {
//        return ResponseEntity.ok("부서 ID 확인 완료: " + deptId);
//    }
}
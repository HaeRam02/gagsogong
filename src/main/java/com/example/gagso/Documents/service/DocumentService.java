package com.example.gagso.Documents.service;

import com.example.gagso.Documents.dto.DocumentDTO;
import com.example.gagso.Documents.helper.DocumentValidator;
import com.example.gagso.Documents.models.Document;
import com.example.gagso.Documents.repository.DocumentRepository;
import com.example.gagso.WorkRoom.dto.TaskDTO;
import com.example.gagso.WorkRoom.dto.TaskListItemDTO;
import com.example.gagso.WorkRoom.models.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentValidator validator;


    @Transactional
    public String register(DocumentDTO dto, MultipartFile file) {
        String validationMessage = validator.validate(dto);
        if (!validationMessage.isEmpty()) {
            return validationMessage;
        }

        Document document = toEntity(dto);

        if (file != null && !file.isEmpty()) {
            String uploadDir = "/path/to/your/uploads/";
            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            File dest = new File(uploadDir + savedFileName);

            try {
                dest.getParentFile().mkdirs();
                file.transferTo(dest);
                //임시
                document.setAttachment(savedFileName);
            } catch (IOException e) {
                e.printStackTrace();
                return "파일 업로드에 실패했습니다.";
            }
        }
//        System.out.println(document.toString());
        documentRepository.save(document);

        return "";
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::toListItemDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> search(String title) {
        return documentRepository.findByCondition(title).stream()
                .map(this::toListItemDTO)
                .collect(Collectors.toList());
    }

    private Document toEntity(DocumentDTO dto) {
        Document document = new Document();
        document.setTitle(dto.getTitle());
        document.setWriterID(dto.getWriterID());
        document.setContent(dto.getContent());
        document.setVisibility(dto.getVisibility());
        document.setDate(dto.getDate());
        return document;
    }

    private DocumentDTO toListItemDTO(Document entity) {
        return new DocumentDTO(
                entity.getDocID(),
                entity.getWriterID(),
                entity.getTitle(),
                entity.getContent(),
                entity.getVisibility(),
                entity.getDate(),
                entity.getAttachment()
        );
    }

    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(String id) {
        return documentRepository.findById(id)
                .map(this::toListItemDTO)
                .orElse(null);
    }
}

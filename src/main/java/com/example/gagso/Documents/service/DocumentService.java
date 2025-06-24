package com.example.gagso.Documents.service;

import com.example.gagso.Documents.dto.AttachmentDTO;
import com.example.gagso.Documents.dto.DocumentDTO;
import com.example.gagso.Documents.helper.DocumentValidator;
import com.example.gagso.Documents.models.Attachment;
import com.example.gagso.Documents.models.Document;
import com.example.gagso.Documents.repository.AttachmentRepository;
import com.example.gagso.Documents.repository.DocumentRepository;
import com.example.gagso.WorkRoom.dto.TaskDTO;
import com.example.gagso.WorkRoom.dto.TaskListItemDTO;
import com.example.gagso.WorkRoom.models.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.gagso.Log.model.ActionType; // ActionType 임포트
import com.example.gagso.Log.service.DocumentLogWriter; // DocumentLogWriter 임포트

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
    private final AttachmentRepository attachmentRepository;
    private final DocumentLogWriter documentLogWriter; // DocumentLogWriter 필드 추가

    @Transactional
    public String register(DocumentDTO dto, List<MultipartFile> files) {
        String validationMessage = validator.validate(dto);
        if (!validationMessage.isEmpty()) {
            return validationMessage;
        }

        Document document = toEntity(dto);

//        if (files != null && !files.isEmpty()) {
//            String uploadDir = "/path/to/your/uploads/";
//            String originalFileName = files.getOriginalFilename();
//            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
//            File dest = new File(uploadDir + savedFileName);
//
//            try {
//                dest.getParentFile().mkdirs();
//                files.transferTo(dest);
//                //임시
//                document.setAttachment(savedFileName);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return "파일 업로드에 실패했습니다.";
//            }
//        }
//        System.out.println(document.toString());
        documentRepository.save(document);
// 4) 첨부파일 저장
        String sysTemp = System.getProperty("java.io.tmpdir");
        File appTempDir = new File(sysTemp, "gagso-uploads");
        if (!appTempDir.exists()) appTempDir.mkdirs();

        // 4) 문서별 폴더 생성
        File docDir = new File(appTempDir, document.getDocID());
        if (!docDir.exists()) docDir.mkdirs();

        // 5) 첨부파일 저장
        if (files != null) {
            for (MultipartFile mf : files) {
                if (mf.isEmpty()) continue;

                // 원본 확장자 추출
                String originalName = mf.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf('.'));
                }
                System.out.println("▶ controller got files: " + (files != null ? files.size() : "null"));

                // 유니크 이름 생성
                String saveName = UUID.randomUUID().toString() + ext;
                File dest = new File(docDir, saveName);

                try {
                    // 파일 쓰기
                    mf.transferTo(dest);

                    // DB에 메타데이터 저장
                    Attachment at = new Attachment();
                    at.setDocument(document);
                    at.setOriginalName(originalName);
                    at.setSaveName(saveName);
                    // 클라이언트에서 다운로드할 URL은 필요에 따라 매핑
                    at.setPath("/temp-uploads/" + document.getDocID() + "/" + saveName);
                    at.setSize(mf.getSize());
                    attachmentRepository.save(at);

                } catch (IOException e) {
                    e.printStackTrace();
                    return "파일 저장 실패: " + originalName;
                }
            }
        }

        String actorId = dto.getWriterID(); // DocumentDTO에서 작성자 ID를 가져옴
        documentLogWriter.save(actorId, ActionType.REGISTER, document);

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
                null
        );
    }

    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(String id) {
        Document document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            return null;
        }
        DocumentDTO dto = toListItemDTO(document);

        // 첨부파일 목록 조회
        List<AttachmentDTO> attachmentDTOs = attachmentRepository
                .findByDocumentDocID(id)
                .stream()
                .map(att -> {
                    AttachmentDTO a = new AttachmentDTO();
                    a.setId(att.getId());
                    a.setOriginalName(att.getOriginalName());
                    a.setPath(att.getPath());   // 프론트에서 download 링크로 사용
                    a.setSize(att.getSize());
                    System.out.println(a);
                    return a;
                })
                .collect(Collectors.toList());
        // 3) DTO 에 설정
        dto.setAttachments(attachmentDTOs);
        if(!attachmentDTOs.isEmpty()) {
            System.out.println("attachment exist");
        }
        return dto;
//        return documentRepository.findById(id)
//                .map(this::toListItemDTO)
//                .orElse(null);
    }
}
package com.example.gagso.Log.service;

import com.example.gagso.Log.model.ActionType;
import com.example.gagso.Log.model.LogEntry;
import com.example.gagso.Log.repository.LogRepository;
import com.example.gagso.Log.service.LogWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentLogWriter implements LogWriter</*Document*/Object> {

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, /*Document*/ Object target) {
        // TODO: Document 서브시스템이 생기면 아래 주석 해제하고 실제 Document 타입으로 변경

        // String documentId = target.getDocumentId();  // ← Document 엔티티에 맞춰 수정
        // String documentType = target.getClass().getSimpleName();

        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);

        // entry.setTargetId(documentId);
        // entry.setTargetType(documentType);

        entry.setTargetId("TEMP_DOCUMENT_ID"); // ← 임시값
        entry.setTargetType("Document");       // ← 고정 문자열

        entry.setTimeStamp(LocalDateTime.now());

        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}

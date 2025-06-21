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
public class EducationLogWriter implements LogWriter</*Education*/Object> {

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, /*Education*/ Object target) {
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);

        // ===== 실제 Education 도메인 적용 시 주석 해제 =====
        // entry.setTargetId(((Education) target).getEducationId());
        // entry.setTargetType(target.getClass().getSimpleName());

        // ===== 현재 임시 대체 코드 =====
        entry.setTargetId("TEMP_EDU_ID");
        entry.setTargetType("Education");
        // ======================================

        entry.setTimeStamp(LocalDateTime.now());
        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}

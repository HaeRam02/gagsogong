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
public class EmployeeLogWriter implements LogWriter</*Employee*/Object> {

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, /*Employee*/ Object target) {
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);

        // ===== TODO: 실제 Employee 엔티티 연결 후 주석 해제 =====
        // entry.setTargetId(((Employee) target).getEmployeeId());
        // entry.setTargetType(target.getClass().getSimpleName());

        // ===== 임시 대체 (서브시스템 구축 전까지) =====
        entry.setTargetId("TEMP_EMPLOYEE_ID");
        entry.setTargetType("Employee");
        // ======================================================

        entry.setTimeStamp(LocalDateTime.now());
        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}

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
public class ScheduleLogWriter implements LogWriter</*Schedule*/Object> {

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, /*Schedule*/ Object target) {
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);

        // ===== 실제 Schedule 서브시스템 구현 시 아래 주석 해제 =====
        // entry.setTargetId(((Schedule) target).getScheduleId());
        // entry.setTargetType(target.getClass().getSimpleName());

        // ===== 현재는 임시 대체 =====
        entry.setTargetId("TEMP_SCHEDULE_ID");
        entry.setTargetType("Schedule");
        // ===============================================

        entry.setTimeStamp(LocalDateTime.now());
        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}

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
public class ClubLogWriter implements LogWriter</*Club*/Object> {

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, /*Club*/ Object target) {
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);

        // ===== 실제 Club 엔티티 도입 후 주석 해제 =====
        // entry.setTargetId(((Club) target).getClubId());
        // entry.setTargetType(target.getClass().getSimpleName());

        // ===== 현재는 임시 코드로 동작 =====
        entry.setTargetId("TEMP_CLUB_ID");
        entry.setTargetType("Club");
        // ===================================

        entry.setTimeStamp(LocalDateTime.now());
        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}

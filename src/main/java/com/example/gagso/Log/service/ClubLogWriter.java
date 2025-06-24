package com.example.gagso.Log.service;

import com.example.gagso.Clubs.models.Club;
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
public class ClubLogWriter implements LogWriter<Club> {

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, Club target) {
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);

        // TaskLogWriter와 동일하게 targetId와 targetType을 설정합니다.
        // Club 엔티티의 실제 ID 필드를 사용합니다.
        entry.setTargetId(target.getClubId());
        // 대상 엔티티의 타입을 문자열 "Club"으로 설정합니다.
        entry.setTargetType("Club");

        entry.setTimeStamp(LocalDateTime.now());
        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}
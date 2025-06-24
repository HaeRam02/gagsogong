package com.example.gagso.Log.service;

import com.example.gagso.Educations.models.Education; // Education 엔티티 임포트
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
public class EducationLogWriter implements LogWriter<Education> { // 제네릭 타입을 Education으로 변경

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, Education target) { // target 타입을 Education으로 변경
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);

        // Education 엔티티의 실제 ID와 타입을 설정합니다.
        entry.setTargetId(target.getEducationId()); // Education 클래스의 getEducationId() 메서드 사용
        entry.setTargetType("Education");           // 대상 엔티티의 타입을 "Education"으로 설정

        entry.setTimeStamp(LocalDateTime.now());

        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}
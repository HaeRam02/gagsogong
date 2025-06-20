package com.example.gagso.Log.service;

import com.example.gagso.Log.model.ActionType;
import com.example.gagso.Log.model.LogEntry;
import com.example.gagso.Log.repository.LogRepository;
import com.example.gagso.Log.service.LogWriter;
import com.example.gagso.WorkRoom.models.Task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskLogWriter implements LogWriter<Task> {

    private final LogRepository logRepository;

    @Override
    public void save(String actor, ActionType action, Task target) {
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID());
        entry.setActorId(actor);
        entry.setActionType(action);
        entry.setTargetType("Task");
        entry.setTargetId(target.getTaskId());  // Task 클래스의 ID 필드
        entry.setTimeStamp(LocalDateTime.now());

        logRepository.save(entry);
    }

    @Override
    public List<LogEntry> getLogList() {
        return logRepository.findAll();
    }
}

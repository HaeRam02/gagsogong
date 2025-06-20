package com.example.gagso.Log.repository;

import com.example.gagso.Log.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<LogEntry, UUID> {
    // save(LogEntry log)와 findAll()은 JpaRepository가 자동 제공
}

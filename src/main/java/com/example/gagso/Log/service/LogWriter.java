package com.example.gagso.Log.service;

import com.example.gagso.Log.model.ActionType;
import com.example.gagso.Log.model.LogEntry;

import java.util.List;

public interface LogWriter<T> {

    void save(String actor, ActionType action, T target);


    List<LogEntry> getLogList();
}

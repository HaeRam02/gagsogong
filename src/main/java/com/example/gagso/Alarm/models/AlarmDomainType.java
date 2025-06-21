package com.example.gagso.Alarm.models;

public enum AlarmDomainType {
    SCHEDULE("일정"),
    TASK("업무"),
    EDUCATION("교육"),
    CLUB("동호회"),
    DOCUMENT("문서");

    private final String description;

    AlarmDomainType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
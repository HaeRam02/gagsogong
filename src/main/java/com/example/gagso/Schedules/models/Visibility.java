package com.example.gagso.Schedules.models;

/**
 * 일정의 공개 범위를 정의하는 Enum
 * 설계 명세: DCD3003
 */
public enum Visibility {
    PUBLIC("전체공개"),
    GROUP("그룹공개"),
    PRIVATE("비공개");

    private final String description;

    Visibility(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
package com.example.gagso.Schedules.dto;

import com.example.gagso.Schedules.models.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자가 입력한 일정 등록 정보를 담는 데이터 전달 객체
 * 설계 명세: DCD3004
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRegisterRequestDTO {

    /**
     * 일정명 (필수항목, 최대 20자)
     */
    private String title;

    /**
     * 일정 설명 (필수항목, 최대 200자)
     */
    private String description;

    /**
     * 시작날짜 (필수항목)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDateTime;

    /**
     * 종료날짜 (필수항목)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDateTime;

    /**
     * 공개범위 (필수항목)
     */
    private Visibility visibility;

    /**
     * 알림여부 (선택항목)
     */
    private Boolean alarmEnabled = false;

    /**
     * 알림시간 (알림여부가 true일 때 필수항목)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    /**
     * 참여자 ID 목록 (선택항목)
     */
    private List<String> participantIds;

    /**
     * 작성자 ID (시스템에서 자동 설정)
     */
    private String employeeId;

    /**
     * 참여자 검색 키워드 (UI에서만 사용, 서버로 전송되지 않음)
     */
    private transient String searchKeyword;

    /**
     * 알림이 설정되어 있는지 확인
     */
    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    /**
     * 그룹 공개 일정인지 확인
     */
    public boolean isGroupVisible() {
        return Visibility.GROUP.equals(visibility);
    }

    /**
     * 참여자가 있는지 확인
     */
    public boolean hasParticipants() {
        return participantIds != null && !participantIds.isEmpty();
    }
}
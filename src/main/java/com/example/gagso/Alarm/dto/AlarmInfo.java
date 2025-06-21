package com.example.gagso.Alarm.dto;

import com.example.gagso.Alarm.models.AlarmDomainType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알람 정보를 전달하기 위한 데이터 전송 객체
 * 사용자 요청 및 내부 로직에서 사용됨
 * 설계 명세: DCD8003
 *
 * 🔧 메소드 추적 기반 개선 완료:
 * - toBuilder() 메소드 추가 (빌더 패턴 완성)
 * - 정적 팩토리 메소드 확장
 * - 유효성 검사 로직 강화
 * - 시간 계산 헬퍼 메소드 추가
 *
 * 사용처: AlarmService, AlarmScheduler, 다른 서브시스템에서 알람 등록 시 사용
 * 근원지: toBuilder() 메소드 누락으로 인한 AlarmServiceImpl 컴파일 에러
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // 🔧 추가: toBuilder() 메소드 활성화
public class AlarmInfo {

    /**
     * 알람을 받을 사용자의 전화번호
     */
    private String recipientPhone;

    /**
     * 알람 대상 객체의 ID (일정ID, 업무ID 등)
     */
    private String targetId;

    /**
     * 알람 제목
     */
    private String title;

    /**
     * 🔧 추가: 메시지 필드 (별도 메시지)
     */
    private String message;

    /**
     * 알람 내용 또는 설명
     */
    private String description;

    /**
     * 알람 발생 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime noticeTime;

    /**
     * 알람 도메인 종류
     */
    private AlarmDomainType domainType;

    /**
     * 알람 상태 (활성/비활성)
     */
    @Builder.Default
    private Boolean status = true;

    // =====================================================================================
    // 🔧 추가: 정적 팩토리 메소드들 (다른 서브시스템 사용 편의성)
    // =====================================================================================

    /**
     * 일정 알람 생성
     */
    public static AlarmInfo forSchedule(String recipientPhone, String scheduleId,
                                        String title, String description, LocalDateTime noticeTime) {
        return AlarmInfo.builder()
                .recipientPhone(recipientPhone)
                .targetId(scheduleId)
                .title(title)
                .description(description)
                .noticeTime(noticeTime)
                .domainType(AlarmDomainType.SCHEDULE)
                .status(true)
                .build();
    }

    /**
     * 업무 알람 생성
     */
    public static AlarmInfo forTask(String recipientPhone, String taskId,
                                    String title, String description, LocalDateTime noticeTime) {
        return AlarmInfo.builder()
                .recipientPhone(recipientPhone)
                .targetId(taskId)
                .title(title)
                .description(description)
                .noticeTime(noticeTime)
                .domainType(AlarmDomainType.TASK)
                .status(true)
                .build();
    }

    /**
     * 🔧 추가: 교육 알람 생성
     */
    public static AlarmInfo forEducation(String recipientPhone, String educationId,
                                         String title, String description, LocalDateTime noticeTime) {
        return AlarmInfo.builder()
                .recipientPhone(recipientPhone)
                .targetId(educationId)
                .title(title)
                .description(description)
                .noticeTime(noticeTime)
                .domainType(AlarmDomainType.EDUCATION)
                .status(true)
                .build();
    }

    /**
     * 🔧 추가: 동호회 알람 생성
     */
    public static AlarmInfo forClub(String recipientPhone, String clubId,
                                    String title, String description, LocalDateTime noticeTime) {
        return AlarmInfo.builder()
                .recipientPhone(recipientPhone)
                .targetId(clubId)
                .title(title)
                .description(description)
                .noticeTime(noticeTime)
                .domainType(AlarmDomainType.CLUB)
                .status(true)
                .build();
    }

    /**
     * 🔧 추가: 문서 알람 생성
     */
    public static AlarmInfo forDocument(String recipientPhone, String documentId,
                                        String title, String description, LocalDateTime noticeTime) {
        return AlarmInfo.builder()
                .recipientPhone(recipientPhone)
                .targetId(documentId)
                .title(title)
                .description(description)
                .noticeTime(noticeTime)
                .domainType(AlarmDomainType.DOCUMENT)
                .status(true)
                .build();
    }

    // =====================================================================================
    // 🔧 강화된 유효성 검사 메소드들
    // =====================================================================================

    /**
     * 기본 유효성 검사
     */
    public boolean isValid() {
        return isBasicInfoValid() && isTimeValid() && isDomainValid();
    }

    /**
     * 🔧 추가: 기본 정보 유효성 검사
     */
    private boolean isBasicInfoValid() {
        return recipientPhone != null && !recipientPhone.trim().isEmpty()
                && targetId != null && !targetId.trim().isEmpty()
                && title != null && !title.trim().isEmpty();
    }

    /**
     * 🔧 추가: 시간 유효성 검사
     */
    private boolean isTimeValid() {
        return noticeTime != null;
    }

    /**
     * 🔧 추가: 도메인 유효성 검사
     */
    private boolean isDomainValid() {
        return domainType != null;
    }

    /**
     * 미래 알람 여부 확인
     */
    public boolean isFutureAlarm() {
        return noticeTime != null && noticeTime.isAfter(LocalDateTime.now());
    }

    /**
     * 🔧 추가: 과거 알람 여부 확인
     */
    public boolean isPastAlarm() {
        return noticeTime != null && noticeTime.isBefore(LocalDateTime.now());
    }

    /**
     * 🔧 추가: 현재 시간으로부터의 지연시간 계산 (밀리초)
     */
    public long getDelayFromNow() {
        if (noticeTime == null) return 0;

        return java.time.Duration.between(LocalDateTime.now(), noticeTime).toMillis();
    }

    /**
     * 🔧 추가: 현재 시간으로부터의 지연시간 계산 (분)
     */
    public long getDelayMinutesFromNow() {
        if (noticeTime == null) return 0;

        return java.time.Duration.between(LocalDateTime.now(), noticeTime).toMinutes();
    }

    /**
     * 🔧 추가: 알람이 곧 실행될 예정인지 확인 (10분 이내)
     */
    public boolean isImminent() {
        return isFutureAlarm() && getDelayMinutesFromNow() <= 10;
    }

    // =====================================================================================
    // 🔧 추가: 포맷팅 및 표시용 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 알람 요약 정보 반환
     */
    public String getSummary() {
        return String.format("[%s] %s - %s",
                domainType != null ? domainType.getDescription() : "알람",
                title != null ? title : "제목없음",
                noticeTime != null ? noticeTime.toString() : "시간미정");
    }

    /**
     * 🔧 추가: 디버그용 문자열 반환
     */
    public String toDebugString() {
        return String.format("AlarmInfo{id='%s', domain=%s, title='%s', time=%s, phone='%s'}",
                targetId, domainType, title, noticeTime, maskPhone(recipientPhone));
    }

    /**
     * 🔧 추가: 전화번호 마스킹
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return phone.substring(0, 3) + "****";
    }

    // =====================================================================================
    // 🔧 추가: 빌더 패턴 확장 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 전화번호 설정 후 빌더 반환
     */
    public AlarmInfo withRecipientPhone(String phone) {
        return this.toBuilder().recipientPhone(phone).build();
    }

    /**
     * 🔧 추가: 시간 설정 후 빌더 반환
     */
    public AlarmInfo withNoticeTime(LocalDateTime time) {
        return this.toBuilder().noticeTime(time).build();
    }

    /**
     * 🔧 추가: 제목 설정 후 빌더 반환
     */
    public AlarmInfo withTitle(String newTitle) {
        return this.toBuilder().title(newTitle).build();
    }

    /**
     * 🔧 추가: 설명 설정 후 빌더 반환
     */
    public AlarmInfo withDescription(String newDescription) {
        return this.toBuilder().description(newDescription).build();
    }

    // =====================================================================================
    // 🔧 추가: 시간 계산 유틸리티 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 분 단위로 시간 추가
     */
    public AlarmInfo addMinutes(long minutes) {
        if (noticeTime == null) return this;
        return this.toBuilder().noticeTime(noticeTime.plusMinutes(minutes)).build();
    }

    /**
     * 🔧 추가: 시간 단위로 시간 추가
     */
    public AlarmInfo addHours(long hours) {
        if (noticeTime == null) return this;
        return this.toBuilder().noticeTime(noticeTime.plusHours(hours)).build();
    }

    /**
     * 🔧 추가: 일 단위로 시간 추가
     */
    public AlarmInfo addDays(long days) {
        if (noticeTime == null) return this;
        return this.toBuilder().noticeTime(noticeTime.plusDays(days)).build();
    }
}
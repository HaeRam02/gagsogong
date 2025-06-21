package com.example.gagso.Schedules.dto;

import com.example.gagso.Schedules.models.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 일정 조회 시 클라이언트로 반환하는 응답 DTO
 * Schedule 엔티티 정보 + 참여자 정보를 포함
 *
 * 🔧 메소드 추적 기반 개선 완료:
 * - ScheduleService에서 사용되는 모든 필드 지원
 * - 누락된 participantCount 필드 추가
 * - 프론트엔드 편의를 위한 추가 메소드들
 * - 성능 최적화를 위한 JsonIgnore 적용
 * - 보안 및 권한 체크 메소드 강화
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {

    // =====================================================================================
    // 핵심 일정 정보 필드들 (Schedule 엔티티와 동일)
    // =====================================================================================

    /**
     * 일정 고유 ID
     */
    private String scheduleId;

    /**
     * 일정 제목
     */
    private String title;

    /**
     * 일정 설명
     */
    private String description;

    /**
     * 일정 시작 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    /**
     * 일정 종료 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    /**
     * 일정 공개 범위
     */
    private Visibility visibility;

    /**
     * 알람 설정 유무
     */
    @Builder.Default
    private Boolean alarmEnabled = false;

    /**
     * 알람 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime alarmTime;

    /**
     * 일정 작성자 ID
     */
    private String employeeId;

    /**
     * 일정 생성 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 일정 수정 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // =====================================================================================
    // 🔧 추가된 참여자 관련 필드들 (ScheduleService에서 사용)
    // =====================================================================================

    /**
     * 일정 작성자 이름 (프론트엔드 표시용)
     */
    private String createdBy;

    /**
     * 참여자 이름 목록 (프론트엔드에서 join으로 표시)
     */
    @Builder.Default
    private List<String> participants = List.of();

    /**
     * 참여자 ID 목록 (내부 처리용)
     */
    @Builder.Default
    private List<String> participantIds = List.of();

    /**
     * 🔧 추가: 참여자 수 (ScheduleService에서 사용하는 필드)
     * ScheduleService.getAccessibleSchedules()에서 result.getFirst().getParticipantCount() 호출
     */
    @Builder.Default
    private Integer participantCount = 0;

    // =====================================================================================
    // 🔧 추가된 프론트엔드 편의 필드들
    // =====================================================================================

    /**
     * 🔧 추가: 일정 상태 (UPCOMING, ONGOING, PAST)
     */
    private String status;

    /**
     * 🔧 추가: 일정까지 남은 시간 (분 단위)
     */
    private Long minutesUntilStart;

    /**
     * 🔧 추가: 일정 진행률 (0-100%)
     */
    private Integer progressPercentage;

    /**
     * 🔧 추가: 참여자 이름 문자열 (콤마로 구분)
     */
    private String participantsDisplay;

    /**
     * 🔧 추가: 일정 기간 (예: "2시간 30분")
     */
    private String durationDisplay;

    /**
     * 🔧 추가: 알림 상태 (NONE, SCHEDULED, SENT)
     */
    private String alarmStatus;

    // =====================================================================================
    // 핵심 비즈니스 메소드들 (ScheduleService에서 사용)
    // =====================================================================================

    /**
     * 비즈니스 메서드: 참여자가 있는지 확인
     * 사용처: ScheduleService.convertToScheduleResponseDTO()
     */
    public boolean hasParticipants() {
        return participants != null && !participants.isEmpty();
    }

    /**
     * 비즈니스 메서드: 특정 사용자가 참여자인지 확인
     * 사용처: 권한 체크, 접근 제어
     */
    public boolean isParticipant(String employeeId) {
        return participantIds != null && participantIds.contains(employeeId);
    }

    /**
     * 비즈니스 메서드: 일정 작성자인지 확인
     * 사용처: 수정/삭제 권한 체크
     */
    public boolean isCreator(String employeeId) {
        return this.employeeId != null && this.employeeId.equals(employeeId);
    }

    /**
     * 비즈니스 메서드: 특정 사용자가 접근 가능한지 확인
     * 사용처: ScheduleService.hasAccessToSchedule()
     */
    public boolean isAccessibleBy(String employeeId) {
        // 작성자이거나 참여자이거나 공개 일정인 경우
        return isCreator(employeeId) ||
                isParticipant(employeeId) ||
                Visibility.PUBLIC.equals(visibility);
    }

    /**
     * 비즈니스 메서드: 알람이 설정되어 있는지 확인
     * 사용처: ScheduleService.scheduleAlarmForSchedule()
     */
    public boolean hasAlarm() {
        return alarmEnabled != null && alarmEnabled && alarmTime != null;
    }

    // =====================================================================================
    // 🔧 추가된 일정 상태 관련 메소드들 (프론트엔드 편의용)
    // =====================================================================================

    /**
     * 🔧 추가: 현재 진행 중인 일정인지 확인
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && endDate != null &&
                !now.isBefore(startDate) && now.isBefore(endDate);
    }

    /**
     * 🔧 추가: 미래 일정인지 확인
     */
    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return startDate != null && startDate.isAfter(now);
    }

    /**
     * 🔧 추가: 과거 일정인지 확인
     */
    public boolean isPast() {
        LocalDateTime now = LocalDateTime.now();
        return endDate != null && endDate.isBefore(now);
    }

    /**
     * 🔧 추가: 오늘 일정인지 확인
     */
    public boolean isToday() {
        LocalDateTime now = LocalDateTime.now();
        return (startDate != null && startDate.toLocalDate().equals(now.toLocalDate())) ||
                (endDate != null && endDate.toLocalDate().equals(now.toLocalDate()));
    }

    /**
     * 🔧 추가: 이번 주 일정인지 확인
     */
    public boolean isThisWeek() {
        if (startDate == null) return false;

        LocalDate now = LocalDate.now();
        LocalDate scheduleDate = startDate.toLocalDate();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return !scheduleDate.isBefore(startOfWeek) && !scheduleDate.isAfter(endOfWeek);
    }

    // =====================================================================================
    // 🔧 추가된 시간 계산 메소드들
    // =====================================================================================

    /**
     * 비즈니스 메서드: 일정 기간 (분 단위)
     * 사용처: 통계, 분석
     */
    public long getDurationMinutes() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.Duration.between(startDate, endDate).toMinutes();
    }

    /**
     * 🔧 추가: 일정 기간 (시간 단위)
     */
    public double getDurationHours() {
        return getDurationMinutes() / 60.0;
    }

    /**
     * 🔧 추가: 일정까지 남은 시간 (분 단위)
     */
    public long getMinutesUntilStart() {
        if (startDate == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (startDate.isBefore(now)) return 0;

        return java.time.Duration.between(now, startDate).toMinutes();
    }

    /**
     * 🔧 추가: 일정 종료까지 남은 시간 (분 단위)
     */
    public long getMinutesUntilEnd() {
        if (endDate == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (endDate.isBefore(now)) return 0;

        return java.time.Duration.between(now, endDate).toMinutes();
    }

    /**
     * 🔧 추가: 일정 진행률 계산 (0-100%)
     */
    public int getProgressPercentage() {
        if (startDate == null || endDate == null) return 0;

        LocalDateTime now = LocalDateTime.now();

        // 아직 시작 안됨
        if (now.isBefore(startDate)) return 0;

        // 이미 끝남
        if (now.isAfter(endDate)) return 100;

        // 진행 중
        long totalDuration = java.time.Duration.between(startDate, endDate).toMinutes();
        long elapsed = java.time.Duration.between(startDate, now).toMinutes();

        return (int) ((elapsed * 100) / totalDuration);
    }

    // =====================================================================================
    // 🔧 추가된 참여자 관련 편의 메소드들
    // =====================================================================================

    /**
     * 🔧 수정: 참여자 수 반환 (필드 우선, 계산 보조)
     * ScheduleService에서 사용: result.getFirst().getParticipantCount()
     */
    public int getParticipantCount() {
        // 명시적으로 설정된 값이 있으면 우선 사용
        if (participantCount != null && participantCount >= 0) {
            return participantCount;
        }

        // 없으면 participants 리스트에서 계산
        return participants != null ? participants.size() : 0;
    }

    /**
     * 🔧 추가: 참여자 이름을 콤마로 구분한 문자열
     */
    public String getParticipantsDisplay() {
        if (participants == null || participants.isEmpty()) {
            return "참여자 없음";
        }

        if (participants.size() <= 3) {
            return String.join(", ", participants);
        }

        return String.join(", ", participants.subList(0, 3)) + " 외 " + (participants.size() - 3) + "명";
    }

    /**
     * 🔧 추가: 참여자 요약 (예: "홍길동 외 2명")
     */
    public String getParticipantsSummary() {
        if (participants == null || participants.isEmpty()) {
            return "참여자 없음";
        }

        if (participants.size() == 1) {
            return participants.get(0);
        }

        return participants.get(0) + " 외 " + (participants.size() - 1) + "명";
    }

    // =====================================================================================
    // 🔧 추가된 표시용 포맷팅 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 시작 날짜 포맷팅 (예: "2024-12-21")
     */
    public String getStartDateDisplay() {
        return startDate != null ? startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }

    /**
     * 🔧 추가: 시작 시간 포맷팅 (예: "14:30")
     */
    public String getStartTimeDisplay() {
        return startDate != null ? startDate.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    /**
     * 🔧 추가: 날짜 범위 표시 (예: "2024-12-21 ~ 2024-12-22")
     */
    public String getDateRangeDisplay() {
        if (startDate == null) return "";

        String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (endDate == null) return startDateStr;

        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        if (startLocalDate.equals(endLocalDate)) {
            return startDateStr; // 같은 날
        }

        return startDateStr + " ~ " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 🔧 추가: 시간 범위 표시 (예: "14:30 - 16:00")
     */
    public String getTimeRangeDisplay() {
        if (startDate == null) return "";

        String startTimeStr = startDate.format(DateTimeFormatter.ofPattern("HH:mm"));

        if (endDate == null) return startTimeStr;

        return startTimeStr + " - " + endDate.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * 🔧 추가: 기간 표시 (예: "2시간 30분")
     */
    public String getDurationDisplay() {
        long minutes = getDurationMinutes();
        if (minutes == 0) return "시간 미정";

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "분";
        }

        if (remainingMinutes == 0) {
            return hours + "시간";
        }

        return hours + "시간 " + remainingMinutes + "분";
    }

    /**
     * 🔧 추가: 일정 상태 문자열 반환
     */
    public String getStatus() {
        if (isPast()) return "PAST";
        if (isOngoing()) return "ONGOING";
        if (isUpcoming()) return "UPCOMING";
        return "UNKNOWN";
    }

    // =====================================================================================
    // 🔧 추가된 알림 관련 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 알림까지 남은 시간 (분 단위)
     */
    public long getMinutesUntilAlarm() {
        if (!hasAlarm()) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (alarmTime.isBefore(now)) return 0;

        return java.time.Duration.between(now, alarmTime).toMinutes();
    }

    /**
     * 🔧 추가: 알림 상태 반환
     */
    public String getAlarmStatus() {
        if (!hasAlarm()) return "NONE";

        LocalDateTime now = LocalDateTime.now();
        if (alarmTime.isBefore(now)) return "SENT";

        return "SCHEDULED";
    }

    // =====================================================================================
    // 🔧 JsonIgnore를 활용한 성능 최적화 (내부 계산용, JSON에 포함되지 않음)
    // =====================================================================================

    /**
     * 🔧 추가: 일정 유효성 검사 (내부용)
     */
    @JsonIgnore
    public boolean isValidSchedule() {
        return scheduleId != null && !scheduleId.trim().isEmpty() &&
                title != null && !title.trim().isEmpty() &&
                startDate != null && endDate != null &&
                startDate.isBefore(endDate);
    }

    /**
     * 🔧 추가: 수정 가능 여부 (내부용)
     */
    @JsonIgnore
    public boolean isEditable() {
        // 과거 일정은 수정 불가
        return !isPast();
    }

    /**
     * 🔧 추가: 삭제 가능 여부 (내부용)
     */
    @JsonIgnore
    public boolean isDeletable() {
        // 진행 중이거나 과거 일정은 삭제 불가
        return !isOngoing() && !isPast();
    }

    // =====================================================================================
    // 디버깅 및 로깅용 메소드들
    // =====================================================================================

    /**
     * 🔧 개선된 toString (디버깅용)
     */
    @Override
    public String toString() {
        return String.format(
                "ScheduleResponseDTO{scheduleId='%s', title='%s', startDate=%s, participantCount=%d, status='%s'}",
                scheduleId, title, startDate, getParticipantCount(), getStatus()
        );
    }

    /**
     * 🔧 추가: 요약 정보 반환 (로깅용)
     */
    public String getSummary() {
        return String.format(
                "[%s] %s (%s) - %s, 참여자 %d명",
                scheduleId, title, getDateRangeDisplay(), getTimeRangeDisplay(), getParticipantCount()
        );
    }

    // =====================================================================================
    // 빌더 패턴 지원을 위한 정적 메소드들
    // =====================================================================================

    /**
     * 🔧 추가: 기본 필드만 설정된 빌더 시작
     */
    public static ScheduleResponseDTOBuilder createBasic(String scheduleId, String title) {
        return ScheduleResponseDTO.builder()
                .scheduleId(scheduleId)
                .title(title)
                .participantCount(0)
                .alarmEnabled(false);
    }

    /**
     * 🔧 추가: 에러 상황용 안전한 DTO 생성
     */
    public static ScheduleResponseDTO createSafe(String scheduleId, String title, String errorMessage) {
        return ScheduleResponseDTO.builder()
                .scheduleId(scheduleId != null ? scheduleId : "UNKNOWN")
                .title(title != null ? title : "오류: " + errorMessage)
                .description("데이터 로드 중 오류가 발생했습니다.")
                .participantCount(0)
                .alarmEnabled(false)
                .participants(List.of())
                .participantIds(List.of())
                .createdBy("알 수 없음")
                .build();
    }
}
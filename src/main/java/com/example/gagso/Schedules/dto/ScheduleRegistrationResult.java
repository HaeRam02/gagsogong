package com.example.gagso.Schedules.dto;

import com.example.gagso.Schedules.models.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사용자가 입력한 일정 등록 결과(성공 여부, 오류 정보)를 포함하는 결과 객체
 * 설계 명세: DCD3014
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRegistrationResult {

    /**
     * 일정 등록 성공 여부
     */
    private boolean result;

    /**
     * 등록된 일정 정보 (성공 시에만 포함)
     * -- GETTER --
     *  등록된 일정을 반환하는 메서드

     */
    private Schedule schedule;

    /**
     * 각 입력 필드에 대해 유효성 검사 결과를 담는 구조화된 결과 객체
     */
    private ValidationResult validationResult;

    /**
     * 일정 등록 성공 여부를 확인하는 메서드
     */
    public boolean isSuccess() {
        return result && validationResult != null && validationResult.isSuccess();
    }

    /**
     * 유효성 검사 결과의 에러를 반환하는 메서드
     */
    public List<ValidationError> getErrors() {
        return validationResult != null ? validationResult.getErrors() : List.of();
    }

    /**
     * 일정 등록이 성공했을 때, 등록된 일정 객체를 포함한 결과 객체를 생성한다.
     */
    public static ScheduleRegistrationResult success(Schedule schedule) {
        return ScheduleRegistrationResult.builder()
                .result(true)
                .schedule(schedule)
                .validationResult(ValidationResult.ofSuccess())
                .build();
    }

    /**
     * 일정 등록이 유효성 검사에 실패했을 때, 오류 정보와 함께 실패 결과 객체를 생성한다.
     */
    public static ScheduleRegistrationResult failure(ScheduleRegisterRequestDTO scheduleDTO, ValidationResult validationResult) {
        return ScheduleRegistrationResult.builder()
                .result(false)
                .schedule(null)
                .validationResult(validationResult)
                .build();
    }

    /**
     * 일정 등록이 실패했을 때, 단일 오류 메시지와 함께 실패 결과 객체를 생성한다.
     */
    public static ScheduleRegistrationResult failure(String field, String message) {
        return ScheduleRegistrationResult.builder()
                .result(false)
                .schedule(null)
                .validationResult(ValidationResult.ofFailure(field, message))
                .build();
    }
}
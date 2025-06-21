package com.example.gagso.Schedules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 각 입력 필드에 대해 유효성 검사 결과(성공여부, 오류필드, 오류메시지)를 담는 구조화된 결과 객체
 * 설계 명세: DCD3013
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {

    /**
     * 유효성 검사 성공 여부
     */
    private boolean result;

    /**
     * 유효성 검사 필드와 해당 에러 메시지를 담는 객체 리스트
     */
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    /**
     * 유효성 검사 결과가 성공인지 확인하는 메서드
     * errors 필드의 isEmpty() 값을 반환
     */
    public boolean isSuccess() {
        return result && errors.isEmpty();
    }

    /**
     * 유효성 검사 결과의 에러를 반환
     */
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * 유효성 검사 결과의 발생 시 해당 error 필드와 에러 내용을 추가
     */
    public void addError(String field, String message) {
        this.errors.add(ValidationError.of(field, message));
        this.result = false;
    }

    /**
     * 성공한 유효성 검사 결과 객체를 생성하는 정적 메서드
     */
    public static ValidationResult ofSuccess() {
        return ValidationResult.builder()
                .result(true)
                .errors(new ArrayList<>())
                .build();
    }

    /**
     * 실패한 유효성 검사 결과 객체를 생성하는 정적 메서드
     */
    public static ValidationResult ofFailure(List<ValidationError> errors) {
        return ValidationResult.builder()
                .result(false)
                .errors(errors != null ? new ArrayList<>(errors) : new ArrayList<>())
                .build();
    }

    /**
     * 단일 오류로 실패한 유효성 검사 결과 객체를 생성하는 정적 메서드
     */
    public static ValidationResult ofFailure(String field, String message) {
        ValidationResult result = ValidationResult.builder()
                .result(false)
                .errors(new ArrayList<>())
                .build();
        result.addError(field, message);
        return result;
    }
}
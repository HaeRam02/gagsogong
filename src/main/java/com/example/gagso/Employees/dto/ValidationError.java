package com.example.gagso.Employees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 유효성 검사 결과 오류(필드, 오류메시지)를 담는 구조화된 결과 객체
 * 설계 명세: DCD3012
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {

    /**
     * 유효성 검사 오류 필드
     */
    private String field;

    /**
     * 유효성 검사 오류 메시지
     */
    private String message;

    /**
     * 편의 생성자
     */
    public static ValidationError of(String field, String message) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .build();
    }
}
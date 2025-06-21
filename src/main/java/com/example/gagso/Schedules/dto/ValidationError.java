package com.example.gagso.Schedules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {


    private String field;


    private String message;

    public static ValidationError of(String field, String message) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .build();
    }
}
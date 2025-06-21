package com.example.gagso.Schedules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {


    private boolean result;


    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();


    public boolean isSuccess() {
        return result && errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }


    public void addError(String field, String message) {
        this.errors.add(ValidationError.of(field, message));
        this.result = false;
    }


    public static ValidationResult ofSuccess() {
        return ValidationResult.builder()
                .result(true)
                .errors(new ArrayList<>())
                .build();
    }


    public static ValidationResult ofFailure(List<ValidationError> errors) {
        return ValidationResult.builder()
                .result(false)
                .errors(errors != null ? new ArrayList<>(errors) : new ArrayList<>())
                .build();
    }


    public static ValidationResult ofFailure(String field, String message) {
        ValidationResult result = ValidationResult.builder()
                .result(false)
                .errors(new ArrayList<>())
                .build();
        result.addError(field, message);
        return result;
    }
}
package com.example.gagso.Clubs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    private boolean valid;
    private List<String> errorFields;
    private String message;

    public static ValidationResult success() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult failure(String message, List<String> errors) {
        return new ValidationResult(false, errors, message);
    }

    public boolean isSuccess() {
        return valid;
    }

    // ✅ addError 추가
    public void addError(String field, String message) {
        if (errorFields == null) {
            errorFields = new ArrayList<>();
        }
        errorFields.add(field + ": " + message);
        this.valid = false;
    }
}
package com.example.gagso.Clubs.helper;

import com.example.gagso.Clubs.dto.ClubRegisterRequestDTO;
import com.example.gagso.Clubs.dto.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClubValidator {

    public ValidationResult validate(ClubRegisterRequestDTO request) {
        List<String> errors = new ArrayList<>();

        if (request.getName() == null || request.getName().isBlank()) errors.add("이름은 필수입니다.");
        if (request.getCreatorName() == null || request.getCreatorName().isBlank()) errors.add("개설자는 필수입니다.");

        return errors.isEmpty()
                ? ValidationResult.success()
                : ValidationResult.failure("유효성 검사 실패", errors);
    }
}

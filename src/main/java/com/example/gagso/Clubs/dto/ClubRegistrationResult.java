package com.example.gagso.Clubs.dto;

import com.example.gagso.Clubs.models.Club;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubRegistrationResult {
    private boolean success;
    private Club club;
    private ValidationResult validationResult;

    public static ClubRegistrationResult success(Club club) {
        return new ClubRegistrationResult(true, club, null);
    }

    public static ClubRegistrationResult failure(Club club, ValidationResult result) {
        return new ClubRegistrationResult(false, club, result);
    }
}
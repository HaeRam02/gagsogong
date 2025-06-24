package com.example.gagso.Clubs.service;

import com.example.gagso.Clubs.dto.ClubRegisterRequestDTO;
import com.example.gagso.Clubs.dto.ClubRegistrationResult;
import com.example.gagso.Clubs.dto.ValidationResult;
import com.example.gagso.Clubs.enums.Visibility;
import com.example.gagso.Clubs.helper.ClubValidator;
import com.example.gagso.Clubs.models.Club;
import com.example.gagso.Clubs.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gagso.Log.model.ActionType; // ActionType 임포트
import com.example.gagso.Log.service.ClubLogWriter; // ClubLogWriter 임포트

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubValidator validator;
    private final ClubLogWriter clubLogWriter; // ClubLogWriter 필드 추가

    @Transactional
    public ClubRegistrationResult registerClub(ClubRegisterRequestDTO request) {
        // ✅ 중복 검사
        if (clubRepository.existsByNameAndCreatorName(
                request.getName(), request.getCreatorName())) {
            ValidationResult duplicateValidation = new ValidationResult();
            duplicateValidation.addError("중복", "같은 이름과 생성자의 동호회가 이미 존재합니다.");
            return ClubRegistrationResult.failure(toEntity(request), duplicateValidation);
        }


        ValidationResult validation = validator.validate(request);
        Club club = toEntity(request);

        if (!validation.isValid()) {
            return ClubRegistrationResult.failure(club, validation);
        }

        Club savedClub = clubRepository.save(club); // 저장된 Club 객체를 받도록 수정

        String actorName = request.getCreatorName();
        clubLogWriter.save(actorName, ActionType.REGISTER, savedClub);

        return ClubRegistrationResult.success(club);
    }

    @Transactional(readOnly = true)
    public List<Club> getClubsSorted(String sort, String username) {
        List<Club> allClubs = clubRepository.findAll();

        List<Club> filtered = allClubs.stream()
                .filter(club -> {
                    if (club.getVisibility() == Visibility.PRIVATE) return false;
                    if (club.getVisibility() == Visibility.GROUP) return club.getCreatorName().equals(username);
                    return true; // PUBLIC
                })
                .collect(Collectors.toList());

        return switch (sort) {
            case "popular" -> filtered.stream()
                    .sorted(Comparator.comparingInt(Club::getMemberCount).reversed())
                    .collect(Collectors.toList());
            case "newest" -> filtered.stream()
                    .sorted(Comparator.comparing(Club::getCreateDate).reversed())
                    .collect(Collectors.toList());
            default -> filtered;
        };
    }

    private Club toEntity(ClubRegisterRequestDTO dto) {
        Club club = new Club();
        club.setName(dto.getName());
        club.setDescription(dto.getDescription());
        club.setCreateDate(dto.getCreateDate());
        club.setVisibility(dto.getVisibility());
        club.setCreatorName(dto.getCreatorName());
        club.setMemberCount(dto.getMemberCount());
        return club;
    }
}
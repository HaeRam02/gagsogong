package com.example.gagso.Clubs.controller;

import com.example.gagso.Clubs.dto.ClubRegisterRequestDTO;
import com.example.gagso.Clubs.dto.ClubRegistrationResult;
import com.example.gagso.Clubs.models.Club;
import com.example.gagso.Clubs.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3004")
public class ClubController {

    private final ClubService clubService;

    @PostMapping("/register")
    public ResponseEntity<?> registerClub(@RequestBody ClubRegisterRequestDTO request) {
        ClubRegistrationResult result = clubService.registerClub(request);
        if (!result.isSuccess()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message", "동호회 등록에 실패했습니다.",
                            "validation", result.getValidationResult()
                    ));
        }
        return ResponseEntity.ok(Map.of(
                "message", "동호회 등록에 성공했습니다.",
                "club", result.getClub()
        ));
    }

    @GetMapping
    public ResponseEntity<List<Club>> getClubs(
            @RequestParam(name = "sort", defaultValue = "default") String sort,
            @RequestParam(name = "userId") String userId
    ) {
        List<Club> clubs = clubService.getClubsSorted(sort, userId);
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/open")
    public ResponseEntity<String> openRegisterScreen(@RequestParam String staffId) {
        return ResponseEntity.ok("동호회 등록 화면 오픈: " + staffId);
    }
}
package com.example.gagso.Clubs.repository;

import com.example.gagso.Clubs.models.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, String> {

    // 기존 메서드 유지 가능
    boolean existsByName(String name);

    // ✅ 중복 체크용 메서드 추가
    boolean existsByNameAndCreatorName(String name, String creatorName);
}
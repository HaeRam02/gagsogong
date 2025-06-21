package com.example.gagso.Documents.repository;

import com.example.gagso.Documents.models.Document;
import com.example.gagso.WorkRoom.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    // 제목 검색
    @Query("SELECT t FROM Document t WHERE t.title LIKE %:title%")
    List<Document> findByCondition(@Param("title") String title);

    // 부서 ID로 업무 검색
//    List<Document> findByDeptId(String deptId);
}

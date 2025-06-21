package com.example.gagso.WorkRoom.repository;

import com.example.gagso.WorkRoom.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    // 제목 검색
    @Query("SELECT t FROM Task t WHERE t.title LIKE %:title%")
    List<Task> findByCondition(@Param("title") String title);

}

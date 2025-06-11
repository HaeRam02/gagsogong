package com.example.gagso.WorkRoom.controller;

import com.example.gagso.WorkRoom.dto.TaskDTO;
import com.example.gagso.WorkRoom.dto.TaskListItemDTO;
import com.example.gagso.WorkRoom.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> registerTask(
            @RequestPart("taskDto") TaskDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String msg = service.register(dto, file);
        if (!msg.isEmpty()) {
            return ResponseEntity.badRequest().body(msg);
        }
        return ResponseEntity.ok("업무가 등록되었습니다.");
    }

    @GetMapping
    public List<TaskListItemDTO> loadAllTasks() {
        return service.getAllTasks();
    }

    @GetMapping("/search")
    public List<TaskListItemDTO> searchTasks(@RequestParam("title") String title) {
        return service.search(title);
    }

    @GetMapping("/open")
    public ResponseEntity<?> openCreateScreen(@RequestParam String deptId) {
        return ResponseEntity.ok("부서 ID 확인 완료: " + deptId);
    }
}
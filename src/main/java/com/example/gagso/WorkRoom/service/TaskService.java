package com.example.gagso.WorkRoom.service;

import com.example.gagso.WorkRoom.dto.TaskDTO;
import com.example.gagso.WorkRoom.dto.TaskListItemDTO;
import com.example.gagso.WorkRoom.helper.TaskValidator;
import com.example.gagso.WorkRoom.models.Task;
import com.example.gagso.WorkRoom.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.gagso.Log.model.ActionType;
import com.example.gagso.Log.service.LogWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskValidator validator;
    private final TaskRepository taskRepository;

    private final LogWriter<Task> taskLogWriter;

    @Transactional
    public String register(TaskDTO dto, MultipartFile file) {
        String validationMessage = validator.validate(dto);
        if (!validationMessage.isEmpty()) {
            return validationMessage;
        }

        Task task = toEntity(dto);

        if (file != null && !file.isEmpty()) {
            String uploadDir = "C:/Users/wodnr/uploads/tasks/";
            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            File dest = new File(uploadDir + savedFileName);

            try {
                dest.getParentFile().mkdirs();
                file.transferTo(dest);
                task.setAttachment(savedFileName);
            } catch (IOException e) {
                e.printStackTrace();
                return "파일 업로드에 실패했습니다.";
            }
        }

        taskRepository.save(task);

        taskLogWriter.save(dto.getManagerId(), ActionType.REGISTER, task);

        return "";
    }

    @Transactional(readOnly = true)
    public List<TaskListItemDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toListItemDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskListItemDTO> search(String title) {
        return taskRepository.findByCondition(title).stream()
                .map(this::toListItemDTO)
                .collect(Collectors.toList());
    }

    private Task toEntity(TaskDTO dto) {
        Task task = new Task();
        task.setTaskId(UUID.randomUUID().toString());
        task.setTitle(dto.getTitle());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setPublic(dto.isPublic());
        task.setAlarmEnabled(dto.isAlarmEnabled());
        task.setPublicStartDate(dto.getPublicStartDate());
        task.setPublicEndDate(dto.getPublicEndDate());
        task.setManagerName(dto.getManagerName());
        task.setManagerId(dto.getManagerId());
        task.setDeptId(dto.getDeptId());
        task.setUnitTask(dto.getUnitTask());

        return task;
    }

    private TaskListItemDTO toListItemDTO(Task entity) {
        return new TaskListItemDTO(
                entity.getTitle(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getManagerName()
        );
    }
}
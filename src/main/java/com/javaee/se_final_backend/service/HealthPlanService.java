package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.entity.Task;
import com.javaee.se_final_backend.model.entity.UserTask;
import com.javaee.se_final_backend.repository.TaskRepository;
import com.javaee.se_final_backend.repository.UserTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.*;

@Service
public class HealthPlanService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserTaskRepository userTaskRepository;

    public List<Map<String, Object>> getTasks(Integer userId) {

        List<UserTask> relations = userTaskRepository.findByUserId(userId);
        List<Integer> taskIds = relations.stream()
                .map(UserTask::getTaskId)
                .toList();

        if (taskIds.isEmpty()) return List.of();

        List<Task> tasks =
                taskRepository.findByTypeAndStatusNotAndIdIn(
                        "sport",
                        "DONE",
                        taskIds
                );

        return tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("title", task.getTitle());
            map.put("status", task.getStatus());
            map.put("time",
                    task.getBeginTime() + " - " + task.getEndTime());
            return map;
        }).toList();
    }

    public void addTaskByDate(Integer userId,
                              String title,
                              String date,
                              String startTime,
                              String endTime) {

        LocalDate d = LocalDate.parse(date);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        Task task = new Task();
        task.setTitle(title);
        task.setType("sport");
        task.setStatus("TODO");
        task.setBeginTime(LocalDateTime.of(d, start));
        task.setEndTime(LocalDateTime.of(d, end));
        task.setProgress(0);

        taskRepository.save(task);

        UserTask ut = new UserTask();
        ut.setUserId(userId);
        ut.setTaskId(task.getId());
        userTaskRepository.save(ut);
    }

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }
}



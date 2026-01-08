package com.javaee.se_final_backend.service;

import com.javaee.se_final_backend.model.entity.Task;
import com.javaee.se_final_backend.model.entity.UserTask;
import com.javaee.se_final_backend.repository.TaskRepository;
import com.javaee.se_final_backend.repository.UserTaskRepository;
import com.javaee.se_final_backend.model.DTO.WeeklyTaskRequest;
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

        List<Task> tasks = taskRepository.findByTypeAndIdIn("sport", taskIds);

        return tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("title", task.getTitle());
            map.put("status", task.getStatus());
            map.put("time",
                    task.getBeginTime() + " - " +
                            task.getEndTime());
            return map;
        }).toList();
    }

    public void addTask(Integer userId, Task task) {
        task.setType("sport");
        taskRepository.save(task);

        UserTask ut = new UserTask();
        ut.setUserId(userId);
        ut.setTaskId(task.getId());
        userTaskRepository.save(ut);
    }

    public void updateTask(Integer id, Task newTask) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setTitle(newTask.getTitle());
        task.setBeginTime(newTask.getBeginTime());
        task.setEndTime(newTask.getEndTime());
        task.setStatus(newTask.getStatus());
        taskRepository.save(task);
    }

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    public void createWeeklyTasks(Integer userId, WeeklyTaskRequest req) {

        LocalDate today = LocalDate.now();

        // 找到“下一个指定星期几”
        int diff = req.getWeekDay() - today.getDayOfWeek().getValue();
        if (diff < 0) diff += 7;
        LocalDate firstDate = today.plusDays(diff);

        LocalTime start = LocalTime.parse(req.getStartTime());
        LocalTime end = LocalTime.parse(req.getEndTime());

        for (int i = 0; i < req.getWeeks(); i++) {

            LocalDate date = firstDate.plusWeeks(i);

            Task task = new Task();
            task.setTitle(req.getTitle());
            task.setType("sport");
            task.setStatus("TODO");
            task.setBeginTime(LocalDateTime.of(date, start));
            task.setEndTime(LocalDateTime.of(date, end));
            task.setProgress(0);

            taskRepository.save(task);

            UserTask ut = new UserTask();
            ut.setUserId(userId);
            ut.setTaskId(task.getId());
            userTaskRepository.save(ut);
        }
    }

}


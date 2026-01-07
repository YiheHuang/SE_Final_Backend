package com.javaee.se_final_backend.controller.health;

import com.javaee.se_final_backend.model.entity.Task;
import com.javaee.se_final_backend.service.TaskService;
import com.javaee.se_final_backend.model.DTO.WeeklyTaskRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health/plan")
@CrossOrigin
public class HealthPlanController {
    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam Integer userId) {
        return taskService.getTasks(userId);
    }

    @PostMapping
    public void add(@RequestParam Integer userId,
                    @RequestBody Task task) {
        taskService.addTask(userId, task);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Integer id,
                       @RequestBody Task task) {
        taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        taskService.deleteTask(id);
    }

    @PostMapping("/weekly")
    public void createWeeklyPlan(@RequestParam Integer userId,
                                 @RequestBody WeeklyTaskRequest req) {
        taskService.createWeeklyTasks(userId, req);
    }
}

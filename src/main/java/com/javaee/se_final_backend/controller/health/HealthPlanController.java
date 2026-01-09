package com.javaee.se_final_backend.controller.health;

import com.javaee.se_final_backend.model.entity.Task;
import com.javaee.se_final_backend.service.HealthPlanService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health/plan")
@CrossOrigin
public class HealthPlanController {

    @Autowired
    private HealthPlanService healthPlanService;

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam Integer userId) {
        return healthPlanService.getTasks(userId);
    }

    @PostMapping
    public void add(@RequestParam Integer userId,
                    @RequestBody Map<String, String> body) {

        healthPlanService.addTaskByDate(
                userId,
                body.get("title"),
                body.get("date"),
                body.get("startTime"),
                body.get("endTime")
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        healthPlanService.deleteTask(id);
    }
}


package com.javaee.se_final_backend.controller.health;

import com.javaee.se_final_backend.service.HealthStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class HealthStatisticsController {

    private final HealthStatisticsService service;

    @GetMapping("/data")
    public Map<String, Object> data(
            @RequestParam Integer userId,
            @RequestParam String begin,
            @RequestParam String end) {

        return service.statistics(
                userId,
                LocalDateTime.parse(begin.replace(" ", "T")),
                LocalDateTime.parse(end.replace(" ", "T"))
        );
    }

    @PostMapping("/ai-report")
    public Map<String, String> aiReport(
            @RequestBody Map<String, String> req) {

        String report = service.generateAIReport(
                Integer.parseInt(req.get("userId")),
                LocalDateTime.parse(req.get("begin").replace(" ", "T")),
                LocalDateTime.parse(req.get("end").replace(" ", "T"))
        );

        return Map.of("report", report);
    }

    @GetMapping("/latest-rating")
    public Map<String, Integer> latestRating(@RequestParam Integer userId) {
        return Map.of("rating", service.getLatestRating(userId));
    }
}



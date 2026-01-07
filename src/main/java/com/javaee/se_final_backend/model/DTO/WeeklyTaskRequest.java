package com.javaee.se_final_backend.model.DTO;

import lombok.Data;

@Data
public class WeeklyTaskRequest {
    private String title;
    private Integer weekDay; // 1=周一 ... 7=周日
    private String startTime; // 08:00
    private String endTime;   // 09:00
    private Integer weeks;
}

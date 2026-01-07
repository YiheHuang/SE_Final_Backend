package com.javaee.se_final_backend.model.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatisticsDTO {
    private String type;
    private Long totalMinutes;
    private Double percentage;
}

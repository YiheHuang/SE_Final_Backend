package com.javaee.se_final_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "HealthStatistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private LocalDateTime beginDate;
    private LocalDateTime endDate;
    private String report;
    private Integer rating;
}


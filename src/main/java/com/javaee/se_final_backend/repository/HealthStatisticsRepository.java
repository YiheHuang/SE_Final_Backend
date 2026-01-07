package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.HealthStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthStatisticsRepository extends JpaRepository<HealthStatistics, Integer>{
    List<HealthStatistics> findByUserIdOrderByBeginDateDesc(Integer userId);

    Optional<HealthStatistics> findFirstByUserIdOrderByIdDesc(Integer userId);
}

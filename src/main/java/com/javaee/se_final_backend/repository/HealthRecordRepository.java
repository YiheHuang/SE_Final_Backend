package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Integer>{
    List<HealthRecord> findByUserIdOrderByDateDesc(Integer userId);

    List<HealthRecord> findByUserIdAndTypeAndDateBetween(
            Integer userId,
            String type,
            LocalDateTime begin,
            LocalDateTime end
    );
}

package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.ExaminationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExaminationReportRepository extends JpaRepository<ExaminationReport, Integer>{
    List<ExaminationReport> findByUserIdOrderByTimeDesc(Integer userId);

    List<ExaminationReport> findTop2ByUserIdOrderByTimeDesc(Integer userId);
}

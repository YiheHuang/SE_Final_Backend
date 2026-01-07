package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Integer>{
    java.util.List<Budget> findByUserId(Integer userId);
    java.util.List<Budget> findByUserIdAndBeginDateBetween(Integer userId, java.time.LocalDateTime begin, java.time.LocalDateTime end);
}

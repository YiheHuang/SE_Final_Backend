package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetItemRepository extends JpaRepository<BudgetItem, Integer>{
    java.util.List<BudgetItem> findByBudgetId(Integer budgetId);
    void deleteByBudgetId(Integer budgetId);
}

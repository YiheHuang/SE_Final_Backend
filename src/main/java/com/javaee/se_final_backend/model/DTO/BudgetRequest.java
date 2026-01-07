package com.javaee.se_final_backend.model.DTO;

import java.math.BigDecimal;
import java.util.List;

public class BudgetRequest {
    private Integer userId;
    private String month; // format YYYY-MM
    private BigDecimal budget;
    private List<CategoryBudget> categories;

    public static class CategoryBudget {
        private String name;
        private BigDecimal budget;
        private BigDecimal spent;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal budget) { this.budget = budget; }

        public BigDecimal getSpent() { return spent; }
        public void setSpent(BigDecimal spent) { this.spent = spent; }
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public List<CategoryBudget> getCategories() { return categories; }
    public void setCategories(List<CategoryBudget> categories) { this.categories = categories; }
}



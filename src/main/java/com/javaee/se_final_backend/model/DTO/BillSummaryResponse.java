package com.javaee.se_final_backend.model.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class BillSummaryResponse {
    private String period;
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private Map<String, BigDecimal> categoryBreakdown;
    private List<Map<String, Object>> dailyTrend;

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getTotalIn() { return totalIn; }
    public void setTotalIn(BigDecimal totalIn) { this.totalIn = totalIn; }

    public BigDecimal getTotalOut() { return totalOut; }
    public void setTotalOut(BigDecimal totalOut) { this.totalOut = totalOut; }

    public Map<String, BigDecimal> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<Map<String, Object>> getDailyTrend() { return dailyTrend; }
    public void setDailyTrend(List<Map<String, Object>> dailyTrend) { this.dailyTrend = dailyTrend; }
}



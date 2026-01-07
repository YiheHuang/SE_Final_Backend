package com.javaee.se_final_backend.model.DTO;

import java.math.BigDecimal;

public class BillItemDTO {
    private String content;
    private String category;
    private BigDecimal price;
    // keep time as String to be resilient to different client formats
    private String time;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}



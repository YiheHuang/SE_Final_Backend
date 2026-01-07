package com.javaee.se_final_backend.model.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class BillCreateRequest {
    private Integer userId;
    private String type;
    private List<BillItemDTO> items;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<BillItemDTO> getItems() { return items; }
    public void setItems(List<BillItemDTO> items) { this.items = items; }
}



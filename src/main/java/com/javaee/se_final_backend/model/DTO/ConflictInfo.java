package com.javaee.se_final_backend.model.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConflictInfo {
    private Integer userId;
    private String userName;
    private String conflictTaskTitle;
    private String conflictTimeRange;

    public String toMessage() {
        return String.format("与%s在%s的「%s」有时间冲突", 
            userName, conflictTimeRange, conflictTaskTitle);
    }
}

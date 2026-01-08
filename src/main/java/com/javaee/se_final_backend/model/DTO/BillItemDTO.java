package com.javaee.se_final_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillItemDTO {
    private String content;
    private String category;
    private BigDecimal price;
    // store as string to accept frontend date/time formats flexibly
    private String time;
}
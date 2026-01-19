package com.kbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {
    private Long id;
    private Long mid;
    private int month;
    private int year;
    private int limitAmount;
    private int usedAmount;
    private LocalDateTime createTime;
}

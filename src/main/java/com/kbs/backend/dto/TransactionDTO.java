package com.kbs.backend.dto;

import com.kbs.backend.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long mid;
    private String type;
    private int amount;
    private String category;
    private String memo;
    private LocalDate date;
    private LocalDateTime createTime;
}

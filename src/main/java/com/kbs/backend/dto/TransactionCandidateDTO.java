package com.kbs.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCandidateDTO {
    @JsonIgnore
    private Long id;
    private LocalDate date;
    private Integer amount;
    private String memo;
    private String category;
    private int number;
    private String type;
}

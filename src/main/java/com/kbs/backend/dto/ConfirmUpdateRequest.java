package com.kbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmUpdateRequest {
    private List<Integer> selectedIndexes;
    private LocalDate newDate;
    private Integer newAmount;
    private String newMemo;
}

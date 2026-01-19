package com.kbs.backend.service;

import com.kbs.backend.domain.Budget;
import com.kbs.backend.domain.Member;
import com.kbs.backend.dto.BudgetDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;

import java.util.List;

public interface BudgetService {
    BudgetDTO getOrCreateMonthlyBudget(Long mid, int defaultLimit);
    void addExpense(Long mid, int amount, int defaultLimit);
    void updateLimit(Long mid, int newLimit);
    void resetMonthlyBudgets(List<Member> members, int defaultLimit);
    PageResponseDTO<BudgetDTO> getBudgets(PageRequestDTO pageRequestDTO, Long mid);
    void createBudget(BudgetDTO budgetDTO);
    void adjustLimit(Long mid, int delta);

    default Budget dtoToEntity(BudgetDTO budgetDTO) {
        Budget budget = Budget.builder()
                .id(budgetDTO.getId())
                .month(budgetDTO.getMonth())
                .year(budgetDTO.getYear())
                .limitAmount(budgetDTO.getLimitAmount())
                .usedAmount(budgetDTO.getUsedAmount())
                .build();
        return budget;
    }

    default BudgetDTO entityToDto(Budget budget) {
        BudgetDTO budgetDTO = BudgetDTO.builder()
                .id(budget.getId())
                .month(budget.getMonth())
                .year(budget.getYear())
                .limitAmount(budget.getLimitAmount())
                .usedAmount(budget.getUsedAmount())
                .mid(budget.getMember().getId())
                .build();
        return budgetDTO;
    }
}

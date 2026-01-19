package com.kbs.backend.controller;

import com.kbs.backend.dto.BudgetDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;

    @GetMapping("/current/{mid}")
    public ResponseEntity<BudgetDTO> getMonthlyBudget(@PathVariable Long mid, @RequestParam(defaultValue = "0") int defaultLimit) {
        BudgetDTO budgetDTO = budgetService.getOrCreateMonthlyBudget(mid, defaultLimit);
        return ResponseEntity.ok(budgetDTO);
    };

    @PostMapping("/expense/{mid}")
    public ResponseEntity<String> addExpense(@PathVariable Long mid, @RequestParam int amount, @RequestParam(defaultValue = "0") int defaultLimit) {
        budgetService.addExpense(mid, amount, defaultLimit);
        return ResponseEntity.ok("Expense added");
    }

    @PutMapping("/limit/{mid}")
    public ResponseEntity<String> updateLimit(@PathVariable Long mid, @RequestParam int newLimit) {
        budgetService.updateLimit(mid, newLimit);
        return ResponseEntity.ok("Limit updated");
    }

    @GetMapping("/list/{mid}")
    public ResponseEntity<PageResponseDTO<BudgetDTO>> getBudgets(@PathVariable Long mid, PageRequestDTO pageRequestDTO) {
        PageResponseDTO<BudgetDTO> result = budgetService.getBudgets(pageRequestDTO, mid);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<String> createBudget(@RequestBody BudgetDTO budgetDTO) {
        budgetService.createBudget(budgetDTO);
        return ResponseEntity.ok("Budget created");
    }

    // 예산 증감 서비스
    @PatchMapping("/limit/{mid}")
    public ResponseEntity<Void> adjustLimit(@PathVariable Long mid, @RequestParam int delta) {
        budgetService.adjustLimit(mid, delta);
        return ResponseEntity.ok().build();
    }
}

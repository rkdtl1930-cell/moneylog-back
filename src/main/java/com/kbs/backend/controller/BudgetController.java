package com.kbs.backend.controller;

import com.kbs.backend.dto.BudgetDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;

    // 안 씀 / 정확하게는 현재 연도의 현재 달의 예산을 가져오기 위한 api
    @GetMapping("/current/{mid}")
    public ResponseEntity<BudgetDTO> getMonthlyBudget(@PathVariable Long mid, @RequestParam(defaultValue = "0") int defaultLimit) {
        BudgetDTO budgetDTO = budgetService.getOrCreateMonthlyBudget(mid, defaultLimit);
        return ResponseEntity.ok(budgetDTO);
    };

    // 안 씀 / 일단 DB 상에는 used_amount를 올리기 위한 api
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
    public ResponseEntity<String> createBudget(
            @RequestBody BudgetDTO budgetDTO,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        budgetService.createBudget(budgetDTO);
        return ResponseEntity.ok("Budget created");
    }

    // 예산 증감 서비스
    @PatchMapping("/limit/{mid}")
    public ResponseEntity<Void> adjustLimit(
            @PathVariable Long mid,
            @RequestParam int delta,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        // 1) 인증 확인(최소)
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        // 2) 본인만(mid 일치) 허용(최소)
        if (!user.getId().equals(mid)) {
            return ResponseEntity.status(403).build();
        }

        // 3) 입력 크기 제한(오류/남용 방지, 복잡도 0)
        if (delta < -200_000_000 || delta > 200_000_000) {
            return ResponseEntity.badRequest().build();
        }

        budgetService.adjustLimit(mid, delta);
        return ResponseEntity.ok().build();
    }
}

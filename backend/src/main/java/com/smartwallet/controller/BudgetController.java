package com.smartwallet.controller;

import com.smartwallet.dto.BudgetDto.*;
import com.smartwallet.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public List<BudgetResponse> getBudgets(@RequestParam(required = false) String period) {
        return budgetService.getBudgetsForPeriod(period);
    }

    @PostMapping
    public BudgetResponse createOrUpdateBudget(@Valid @RequestBody BudgetRequest request) {
        return budgetService.createOrUpdateBudget(request);
    }

    @DeleteMapping("/{id}")
    public void deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
    }
}

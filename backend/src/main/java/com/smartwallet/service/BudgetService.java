package com.smartwallet.service;

import com.smartwallet.dto.BudgetDto.*;
import com.smartwallet.entity.*;
import com.smartwallet.exception.ResourceNotFoundException;
import com.smartwallet.repository.BudgetRepository;
import com.smartwallet.repository.CategoryRepository;
import com.smartwallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;

    public List<BudgetResponse> getBudgetsForPeriod(String period) {
        User user = currentUserProvider.getCurrentUser();
        String targetPeriod = period != null ? period : currentPeriod();

        return budgetRepository.findByUserIdAndPeriod(user.getId(), targetPeriod)
                .stream().map(b -> toDto(b, user.getId())).toList();
    }

    public BudgetResponse createOrUpdateBudget(BudgetRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Budget budget = budgetRepository
                .findByUserIdAndCategoryIdAndPeriod(user.getId(), category.getId(), request.period())
                .orElse(Budget.builder()
                        .user(user)
                        .category(category)
                        .period(request.period())
                        .build());

        budget.setMonthlyLimit(request.monthlyLimit());
        budget = budgetRepository.save(budget);

        return toDto(budget, user.getId());
    }

    public void deleteBudget(Long id) {
        User user = currentUserProvider.getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Budget not found");
        }
        budgetRepository.delete(budget);
    }

    private BudgetResponse toDto(Budget budget, Long userId) {
        YearMonth ym = YearMonth.parse(budget.getPeriod());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal spent = transactionRepository
                .findByUserIdAndCategoryIdAndTransactionDateBetween(userId, budget.getCategory().getId(), start, end)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        double percentUsed = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getMonthlyLimit(),
                spent,
                remaining,
                percentUsed,
                budget.getPeriod(),
                remaining.compareTo(BigDecimal.ZERO) < 0
        );
    }

    private String currentPeriod() {
        return YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}

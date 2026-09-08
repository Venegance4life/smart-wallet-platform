package com.smartwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BudgetDto {

    public record BudgetRequest(
            @NotNull(message = "Category is required") Long categoryId,
            @NotNull(message = "Monthly limit is required") @Positive(message = "Limit must be positive") BigDecimal monthlyLimit,
            @NotBlank(message = "Period is required")
            @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Period must be in YYYY-MM format")
            String period
    ) {}

    public record BudgetResponse(
            Long id,
            Long categoryId,
            String categoryName,
            BigDecimal monthlyLimit,
            BigDecimal spent,
            BigDecimal remaining,
            double percentUsed,
            String period,
            boolean overBudget
    ) {}
}

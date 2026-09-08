package com.smartwallet.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsDto {

    public record CategoryBreakdown(String category, BigDecimal amount, double percentage) {}

    public record MonthlyTrend(String month, BigDecimal income, BigDecimal expense, BigDecimal net) {}

    public record AnalyticsSummary(
            BigDecimal totalBalance,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal savingsRate,
            List<CategoryBreakdown> expenseByCategory,
            List<MonthlyTrend> monthlyTrends
    ) {}
}

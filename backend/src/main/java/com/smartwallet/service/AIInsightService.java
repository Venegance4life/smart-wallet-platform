package com.smartwallet.service;

import com.smartwallet.dto.AIInsightDto;
import com.smartwallet.entity.Transaction;
import com.smartwallet.entity.TransactionType;
import com.smartwallet.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Rule-based "AI" insight engine. Applies statistical heuristics (moving
 * averages, variance thresholds, budget-vs-actual comparison) over the
 * user's transaction history to surface spending anomalies, savings tips,
 * and a simple next-month forecast.
 *
 * This is intentionally dependency-free so the project runs out of the box.
 * To upgrade it to call a real LLM (e.g. the Anthropic API) for richer,
 * natural-language narratives, inject a WebClient/RestClient here, build a
 * prompt from the same aggregates below, and merge the model's response
 * into the returned list.
 */
@Service
@RequiredArgsConstructor
public class AIInsightService {

    private final com.smartwallet.repository.TransactionRepository transactionRepository;
    private final com.smartwallet.repository.BudgetRepository budgetRepository;
    private final CurrentUserProvider currentUserProvider;

    private static final BigDecimal ANOMALY_THRESHOLD = BigDecimal.valueOf(1.4); // 40% above average

    public List<AIInsightDto> generateInsights() {
        User user = currentUserProvider.getCurrentUser();
        List<AIInsightDto> insights = new ArrayList<>();

        List<Transaction> all = transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId());
        if (all.isEmpty()) {
            insights.add(new AIInsightDto(
                    "TIP",
                    "Welcome to your Smart Wallet",
                    "Add a few transactions and I'll start surfacing personalized spending insights and forecasts.",
                    "LOW"));
            return insights;
        }

        insights.addAll(detectCategoryAnomalies(user.getId()));
        insights.addAll(checkBudgetOverruns(user.getId()));
        insights.addAll(savingsRateTip(all));
        insights.add(forecastNextMonth(user.getId()));

        return insights;
    }

    /** Compares this month's spend per category against the trailing 3-month average. */
    private List<AIInsightDto> detectCategoryAnomalies(Long userId) {
        List<AIInsightDto> results = new ArrayList<>();
        YearMonth current = YearMonth.now();

        Map<String, BigDecimal> currentMonthByCategory = spendByCategory(userId, current);
        Map<String, List<BigDecimal>> historyByCategory = new HashMap<>();

        for (int i = 1; i <= 3; i++) {
            Map<String, BigDecimal> monthSpend = spendByCategory(userId, current.minusMonths(i));
            monthSpend.forEach((cat, amt) ->
                    historyByCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(amt));
        }

        for (Map.Entry<String, BigDecimal> entry : currentMonthByCategory.entrySet()) {
            String category = entry.getKey();
            BigDecimal currentSpend = entry.getValue();
            List<BigDecimal> history = historyByCategory.getOrDefault(category, List.of());

            if (history.isEmpty() || currentSpend.compareTo(BigDecimal.valueOf(20)) < 0) {
                continue; // not enough history, or too small to matter
            }

            BigDecimal avg = history.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);

            if (avg.compareTo(BigDecimal.ZERO) > 0 &&
                    currentSpend.compareTo(avg.multiply(ANOMALY_THRESHOLD)) > 0) {
                double increasePct = currentSpend.subtract(avg)
                        .divide(avg, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                results.add(new AIInsightDto(
                        "ANOMALY",
                        category + " spending spike",
                        String.format("You've spent %.0f%% more on %s this month ($%.2f) than your recent average ($%.2f).",
                                increasePct, category, currentSpend, avg),
                        increasePct > 80 ? "HIGH" : "MEDIUM"
                ));
            }
        }

        return results;
    }

    private List<AIInsightDto> checkBudgetOverruns(Long userId) {
        List<AIInsightDto> results = new ArrayList<>();
        String period = YearMonth.now().toString();

        budgetRepository.findByUserIdAndPeriod(userId, period).forEach(budget -> {
            LocalDate start = YearMonth.parse(period).atDay(1);
            LocalDate end = YearMonth.parse(period).atEndOfMonth();

            BigDecimal spent = transactionRepository
                    .findByUserIdAndCategoryIdAndTransactionDateBetween(userId, budget.getCategory().getId(), start, end)
                    .stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (spent.compareTo(budget.getMonthlyLimit()) > 0) {
                results.add(new AIInsightDto(
                        "WARNING",
                        budget.getCategory().getName() + " budget exceeded",
                        String.format("You've spent $%.2f of your $%.2f budget for %s this month.",
                                spent, budget.getMonthlyLimit(), budget.getCategory().getName()),
                        "HIGH"
                ));
            } else if (spent.compareTo(budget.getMonthlyLimit().multiply(BigDecimal.valueOf(0.85))) > 0) {
                results.add(new AIInsightDto(
                        "WARNING",
                        "Approaching " + budget.getCategory().getName() + " limit",
                        String.format("You're at %.0f%% of your %s budget with time left in the month.",
                                spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100,
                                budget.getCategory().getName()),
                        "MEDIUM"
                ));
            }
        });

        return results;
    }

    private List<AIInsightDto> savingsRateTip(List<Transaction> all) {
        BigDecimal income = all.stream().filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = all.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        double savingsRate = income.subtract(expense).divide(income, 4, RoundingMode.HALF_UP).doubleValue() * 100;

        String title;
        String message;
        String severity;

        if (savingsRate < 0) {
            title = "You're spending more than you earn";
            message = "Your expenses currently exceed your income. Consider reviewing your top spending categories.";
            severity = "HIGH";
        } else if (savingsRate < 10) {
            title = "Low savings rate";
            message = String.format("You're saving about %.0f%% of your income. Financial experts often recommend aiming for 20%%.", savingsRate);
            severity = "MEDIUM";
        } else {
            title = "Healthy savings rate";
            message = String.format("Nice work — you're saving roughly %.0f%% of your income overall.", savingsRate);
            severity = "LOW";
        }

        return List.of(new AIInsightDto("TIP", title, message, severity));
    }

    /** Simple 3-month moving average forecast for next month's expenses. */
    private AIInsightDto forecastNextMonth(Long userId) {
        YearMonth current = YearMonth.now();
        List<BigDecimal> lastThree = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            YearMonth ym = current.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            BigDecimal spend = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end)
                    .stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (spend.compareTo(BigDecimal.ZERO) > 0) {
                lastThree.add(spend);
            }
        }

        if (lastThree.isEmpty()) {
            return new AIInsightDto("FORECAST", "Not enough data yet",
                    "Once you've logged a full month of expenses, I can forecast next month's spending.", "LOW");
        }

        BigDecimal forecast = lastThree.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(lastThree.size()), 2, RoundingMode.HALF_UP);

        return new AIInsightDto(
                "FORECAST",
                "Next month's spending forecast",
                String.format("Based on your recent trend, expect to spend around $%.2f next month.", forecast),
                "LOW"
        );
    }

    private Map<String, BigDecimal> spendByCategory(Long userId, YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        Map<String, BigDecimal> result = new HashMap<>();

        transactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .forEach(t -> {
                    String name = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
                    result.merge(name, t.getAmount(), BigDecimal::add);
                });

        return result;
    }
}

package com.smartwallet.service;

import com.smartwallet.dto.AnalyticsDto.*;
import com.smartwallet.entity.Transaction;
import com.smartwallet.entity.TransactionType;
import com.smartwallet.entity.User;
import com.smartwallet.entity.Wallet;
import com.smartwallet.exception.ResourceNotFoundException;
import com.smartwallet.repository.TransactionRepository;
import com.smartwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CurrentUserProvider currentUserProvider;

    public AnalyticsSummary getSummary() {
        User user = currentUserProvider.getCurrentUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        BigDecimal totalIncome = transactionRepository.sumIncomeForUser(user.getId());
        BigDecimal totalExpense = transactionRepository.sumExpenseForUser(user.getId());

        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = totalIncome.subtract(totalExpense)
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<CategoryBreakdown> breakdown = buildCategoryBreakdown(user.getId(), totalExpense);
        List<MonthlyTrend> trends = buildMonthlyTrends(user.getId());

        return new AnalyticsSummary(wallet.getBalance(), totalIncome, totalExpense, savingsRate, breakdown, trends);
    }

    private List<CategoryBreakdown> buildCategoryBreakdown(Long userId, BigDecimal totalExpense) {
        List<Transaction> expenses = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
                .stream().filter(t -> t.getType() == TransactionType.EXPENSE).toList();

        Map<String, BigDecimal> grouped = new LinkedHashMap<>();
        for (Transaction t : expenses) {
            String name = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
            grouped.merge(name, t.getAmount(), BigDecimal::add);
        }

        return grouped.entrySet().stream()
                .map(e -> new CategoryBreakdown(
                        e.getKey(),
                        e.getValue(),
                        totalExpense.compareTo(BigDecimal.ZERO) > 0
                                ? e.getValue().divide(totalExpense, 4, RoundingMode.HALF_UP).doubleValue() * 100
                                : 0
                ))
                .sorted((a, b) -> b.amount().compareTo(a.amount()))
                .collect(Collectors.toList());
    }

    private List<MonthlyTrend> buildMonthlyTrends(Long userId) {
        List<MonthlyTrend> trends = new ArrayList<>();
        YearMonth current = YearMonth.now();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<Transaction> monthTx = transactionRepository
                    .findByUserIdAndTransactionDateBetween(userId, start, end);

            BigDecimal income = monthTx.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = monthTx.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.add(new MonthlyTrend(ym.format(labelFormatter), income, expense, income.subtract(expense)));
        }

        return trends;
    }
}

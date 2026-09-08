package com.smartwallet.config;

import com.smartwallet.entity.Category;
import com.smartwallet.entity.TransactionType;
import com.smartwallet.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        seed("Salary", TransactionType.INCOME, "briefcase");
        seed("Freelance", TransactionType.INCOME, "laptop");
        seed("Investments", TransactionType.INCOME, "trending-up");
        seed("Other Income", TransactionType.INCOME, "plus-circle");

        seed("Groceries", TransactionType.EXPENSE, "shopping-cart");
        seed("Rent", TransactionType.EXPENSE, "home");
        seed("Utilities", TransactionType.EXPENSE, "zap");
        seed("Transportation", TransactionType.EXPENSE, "car");
        seed("Dining Out", TransactionType.EXPENSE, "coffee");
        seed("Entertainment", TransactionType.EXPENSE, "film");
        seed("Healthcare", TransactionType.EXPENSE, "heart");
        seed("Shopping", TransactionType.EXPENSE, "shopping-bag");
        seed("Subscriptions", TransactionType.EXPENSE, "repeat");
        seed("Travel", TransactionType.EXPENSE, "map-pin");
        seed("Other Expense", TransactionType.EXPENSE, "more-horizontal");
    }

    private void seed(String name, TransactionType type, String icon) {
        categoryRepository.save(Category.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .user(null) // global/system category
                .build());
    }
}

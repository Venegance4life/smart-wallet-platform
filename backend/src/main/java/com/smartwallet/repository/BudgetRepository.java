package com.smartwallet.repository;

import com.smartwallet.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndPeriod(Long userId, String period);
    Optional<Budget> findByUserIdAndCategoryIdAndPeriod(Long userId, Long categoryId, String period);
}

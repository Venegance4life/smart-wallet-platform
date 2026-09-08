package com.smartwallet.repository;

import com.smartwallet.entity.Transaction;
import com.smartwallet.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);

    List<Transaction> findByUserIdAndCategoryIdAndTransactionDateBetween(
            Long userId, Long categoryId, LocalDate start, LocalDate end);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.id = :userId
              AND (:type IS NULL OR t.type = :type)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:startDate IS NULL OR t.transactionDate >= :startDate)
              AND (:endDate IS NULL OR t.transactionDate <= :endDate)
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    Page<Transaction> findFiltered(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.user.id = :userId and t.type = 'INCOME'
            """)
    BigDecimal sumIncomeForUser(@Param("userId") Long userId);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.user.id = :userId and t.type = 'EXPENSE'
            """)
    BigDecimal sumExpenseForUser(@Param("userId") Long userId);
}

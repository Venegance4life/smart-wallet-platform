package com.smartwallet.service;

import com.smartwallet.dto.TransactionDto.*;
import com.smartwallet.entity.*;
import com.smartwallet.exception.BadRequestException;
import com.smartwallet.exception.ResourceNotFoundException;
import com.smartwallet.repository.CategoryRepository;
import com.smartwallet.repository.TransactionRepository;
import com.smartwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final WalletRepository walletRepository;
    private final CurrentUserProvider currentUserProvider;

    public List<TransactionResponse> getMyTransactions() {
        User user = currentUserProvider.getCurrentUser();
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream().map(this::toDto).toList();
    }

    public Map<String, Object> getFilteredTransactions(
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;

        User user = currentUserProvider.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> result = transactionRepository.findFiltered(
                user.getId(), type, categoryId, startDate, endDate, pageable);

        return Map.of(
                "content", result.getContent().stream().map(this::toDto).toList(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "last", result.isLast()
        );
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = currentUserProvider.getCurrentUser();
        validateRequest(request);

        Category category = resolveCategory(request.categoryId(), user);

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .type(request.type())
                .amount(request.amount())
                .description(request.description())
                .merchant(request.merchant())
                .transactionDate(request.transactionDate())
                .build();

        transaction = transactionRepository.save(transaction);
        adjustWalletBalance(user.getId(), request.type(), request.amount(), true);

        return toDto(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User user = currentUserProvider.getCurrentUser();
        validateRequest(request);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        // Reverse previous balance impact
        adjustWalletBalance(user.getId(), transaction.getType(), transaction.getAmount(), false);

        Category category = resolveCategory(request.categoryId(), user);

        transaction.setType(request.type());
        transaction.setAmount(request.amount());
        transaction.setCategory(category);
        transaction.setDescription(request.description());
        transaction.setMerchant(request.merchant());
        transaction.setTransactionDate(request.transactionDate());

        transaction = transactionRepository.save(transaction);

        // Apply new balance impact
        adjustWalletBalance(user.getId(), request.type(), request.amount(), true);

        return toDto(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User user = currentUserProvider.getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        // Reverse the balance effect
        adjustWalletBalance(user.getId(), transaction.getType(), transaction.getAmount(), false);

        transactionRepository.delete(transaction);
    }

    /**
     * @param apply true = apply the transaction effect, false = reverse it
     */
    private void adjustWalletBalance(Long userId, TransactionType type, BigDecimal amount, boolean apply) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        BigDecimal delta;
        if (type == TransactionType.INCOME) {
            delta = apply ? amount : amount.negate();
        } else {
            delta = apply ? amount.negate() : amount;
        }

        wallet.setBalance(wallet.getBalance().add(delta));
        walletRepository.save(wallet);
    }

    private Category resolveCategory(Long categoryId, User user) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Must be a system category or owned by this user
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private void validateRequest(TransactionRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        if (request.transactionDate() != null && request.transactionDate().isAfter(LocalDate.now().plusDays(1))) {
            throw new BadRequestException("Transaction date cannot be more than one day in the future");
        }
    }

    private TransactionResponse toDto(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : "Uncategorized",
                t.getDescription(),
                t.getMerchant(),
                t.getTransactionDate()
        );
    }
}

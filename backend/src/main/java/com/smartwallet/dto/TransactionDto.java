package com.smartwallet.dto;

import com.smartwallet.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {

    public record TransactionRequest(
            @NotNull(message = "Type is required") TransactionType type,
            @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
            Long categoryId,
            @Size(max = 500) String description,
            @Size(max = 200) String merchant,
            @NotNull(message = "Transaction date is required") LocalDate transactionDate
    ) {}

    public record TransactionResponse(
            Long id,
            TransactionType type,
            BigDecimal amount,
            Long categoryId,
            String categoryName,
            String description,
            String merchant,
            LocalDate transactionDate
    ) {}
}

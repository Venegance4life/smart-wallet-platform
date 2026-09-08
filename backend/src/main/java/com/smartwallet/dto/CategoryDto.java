package com.smartwallet.dto;

import com.smartwallet.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CategoryDto {

    public record CategoryRequest(
            @NotBlank String name,
            @NotNull TransactionType type,
            String icon
    ) {}

    public record CategoryResponse(
            Long id,
            String name,
            TransactionType type,
            String icon,
            boolean system
    ) {}
}

package com.smartwallet.dto;

import java.math.BigDecimal;

public record WalletDto(Long id, BigDecimal balance, String currency) {}

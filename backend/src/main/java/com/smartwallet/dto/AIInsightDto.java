package com.smartwallet.dto;

public record AIInsightDto(
        String type,       // e.g. "WARNING", "TIP", "FORECAST", "ANOMALY"
        String title,
        String message,
        String severity     // "LOW" | "MEDIUM" | "HIGH"
) {}

package com.smartwallet.controller;

import com.smartwallet.dto.AnalyticsDto.AnalyticsSummary;
import com.smartwallet.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public AnalyticsSummary getSummary() {
        return analyticsService.getSummary();
    }
}

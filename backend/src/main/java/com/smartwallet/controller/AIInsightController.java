package com.smartwallet.controller;

import com.smartwallet.dto.AIInsightDto;
import com.smartwallet.service.AIInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class AIInsightController {

    private final AIInsightService aiInsightService;

    @GetMapping
    public List<AIInsightDto> getInsights() {
        return aiInsightService.generateInsights();
    }
}

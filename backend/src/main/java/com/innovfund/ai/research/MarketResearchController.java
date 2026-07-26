package com.innovfund.ai.research;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/market-research")
@RequiredArgsConstructor
public class MarketResearchController {

    private final MarketResearchService marketResearchService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public MarketResearchResult research(@Valid @RequestBody MarketResearchRequest request) {
        return marketResearchService.research(request.query());
    }
}

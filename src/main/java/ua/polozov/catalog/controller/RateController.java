package ua.polozov.catalog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.polozov.catalog.service.RateService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rate")
@Tag(name = "Exchange Rate", description = "Operations for managing EUR/UAH exchange rate")
public class RateController {

    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @Operation(summary = "Get current exchange rate", description = "Returns the current EUR to UAH exchange rate")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCurrentRate() {
        BigDecimal rate = rateService.getCurrentRate();
        if (rate == null) {
            return ResponseEntity.ok(Map.of("message", "No rate available yet"));
        }
        return ResponseEntity.ok(Map.of("rate", rate, "currency", "EUR/UAH"));
    }

    @Operation(summary = "Fetch rate from NBU", description = "Manually fetches the current rate from NBU API (for testing)")
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchRateFromNbu() {
        BigDecimal rate = rateService.fetchRateFromNbu();
        if (rate == null) {
            return ResponseEntity.ok(Map.of("error", "Failed to fetch rate from NBU"));
        }
        return ResponseEntity.ok(Map.of("rate", rate, "source", "NBU API"));
    }

    @Operation(summary = "Update rate manually", description = "Manually updates the exchange rate and recalculates book prices")
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateRate(@RequestParam BigDecimal rate) {
        rateService.updateRate(rate);
        return ResponseEntity.ok(Map.of("message", "Rate updated successfully", "rate", rate));
    }
}


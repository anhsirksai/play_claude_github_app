package com.settlement.service;

import com.settlement.model.FXRateQuote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FXRateServiceTest {

    private FXRateService fxRateService;

    @BeforeEach
    void setUp() {
        fxRateService = new FXRateService();
    }

    @Test
    @DisplayName("Should return 1.0 when source and target currencies are identical")
    void testSameCurrency() {
        BigDecimal rate = fxRateService.getOptimalFXRate("USD", "USD", Instant.now());
        assertEquals(BigDecimal.ONE, rate);
    }

    @Test
    @DisplayName("Should select lowest rate within 10-minute window for user benefit")
    void testOptimalRateSelectionInTenMinuteWindow() {
        Instant baseTime = Instant.parse("2026-10-04T10:00:00Z");

        // Quotes in sequence
        fxRateService.addRateQuote(new FXRateQuote("USD", "EUR", new BigDecimal("0.9200"), baseTime)); // at T
        fxRateService.addRateQuote(new FXRateQuote("USD", "EUR", new BigDecimal("0.9150"), baseTime.plus(3, ChronoUnit.MINUTES))); // at T+3m (lower!)
        fxRateService.addRateQuote(new FXRateQuote("USD", "EUR", new BigDecimal("0.9300"), baseTime.plus(7, ChronoUnit.MINUTES))); // at T+7m (higher)
        fxRateService.addRateQuote(new FXRateQuote("USD", "EUR", new BigDecimal("0.9000"), baseTime.plus(15, ChronoUnit.MINUTES))); // at T+15m (outside window)

        BigDecimal optimalRate = fxRateService.getOptimalFXRate("USD", "EUR", baseTime);

        // Optimal rate must be 0.9150 (the minimum rate within the 10-minute window [10:00, 10:10])
        assertEquals(new BigDecimal("0.9150"), optimalRate);
    }

    @Test
    @DisplayName("Should throw IllegalStateException if no rate quote available")
    void testNoRateAvailable() {
        assertThrows(IllegalStateException.class, () ->
            fxRateService.getOptimalFXRate("USD", "GBP", Instant.now())
        );
    }
}

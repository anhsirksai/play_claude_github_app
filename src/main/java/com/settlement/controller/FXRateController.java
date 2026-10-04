package com.settlement.controller;

import com.settlement.model.FXRateQuote;
import com.settlement.service.FXRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/rates")
public class FXRateController {

    private final FXRateService fxRateService;

    public FXRateController(FXRateService fxRateService) {
        this.fxRateService = fxRateService;
    }

    @PostMapping
    public ResponseEntity<FXRateQuote> recordRate(@RequestBody FXRateQuote quote) {
        fxRateService.addRateQuote(quote);
        return ResponseEntity.status(HttpStatus.CREATED).body(quote);
    }

    @GetMapping("/optimal")
    public ResponseEntity<BigDecimal> getOptimalRate(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam(required = false) String timestamp) {
        Instant reqTime = (timestamp != null) ? Instant.parse(timestamp) : Instant.now();
        BigDecimal rate = fxRateService.getOptimalFXRate(fromCurrency, toCurrency, reqTime);
        return ResponseEntity.ok(rate);
    }
}

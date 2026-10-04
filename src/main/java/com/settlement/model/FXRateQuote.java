package com.settlement.model;

import java.math.BigDecimal;
import java.time.Instant;

public record FXRateQuote(
    String fromCurrency,
    String toCurrency,
    BigDecimal rate,
    Instant timestamp
) {}

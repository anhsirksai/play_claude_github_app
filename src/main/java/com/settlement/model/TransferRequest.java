package com.settlement.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferRequest(
    String id,
    String sourceAccountId,
    String targetAccountId,
    BigDecimal sendAmount,
    String sourceCurrency,
    String targetCurrency,
    Instant timestamp
) {}

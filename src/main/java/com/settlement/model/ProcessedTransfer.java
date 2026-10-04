package com.settlement.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ProcessedTransfer(
    String transferId,
    String sourceAccountId,
    String targetAccountId,
    BigDecimal sendAmount,
    String sourceCurrency,
    BigDecimal targetAmount,
    String targetCurrency,
    BigDecimal appliedFXRate,
    FeeBreakdown feeBreakdown,
    Instant timestamp
) {}

package com.settlement.model;

import java.math.BigDecimal;

public record FeeBreakdown(
    BigDecimal fixedFee,
    BigDecimal percentageFee,
    BigDecimal totalFee
) {}

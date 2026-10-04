package com.settlement.model;

import java.math.BigDecimal;

public record NetDebtObligation(
    String debtorAccountId,
    String creditorAccountId,
    String currency,
    BigDecimal netAmount
) {}

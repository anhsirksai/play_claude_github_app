package com.settlement.service;

import com.settlement.model.FeeBreakdown;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FeeReconciliationEngine {

    private static final BigDecimal FIXED_FEE = new BigDecimal("2.50");
    private static final BigDecimal TIER1_RATE = new BigDecimal("0.0050"); // 0.5% below 10,000
    private static final BigDecimal TIER2_RATE = new BigDecimal("0.0025"); // 0.25% at or above 10,000
    private static final BigDecimal TIER_THRESHOLD = new BigDecimal("10000.00");

    public FeeBreakdown calculateFee(BigDecimal sendAmount) {
        BigDecimal percentageRate = (sendAmount.compareTo(TIER_THRESHOLD) >= 0) ? TIER2_RATE : TIER1_RATE;
        BigDecimal percentageFee = sendAmount.multiply(percentageRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalFee = FIXED_FEE.add(percentageFee).setScale(2, RoundingMode.HALF_UP);

        return new FeeBreakdown(FIXED_FEE, percentageFee, totalFee);
    }
}

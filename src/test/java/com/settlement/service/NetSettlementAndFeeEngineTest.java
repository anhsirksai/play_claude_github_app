package com.settlement.service;

import com.settlement.model.FeeBreakdown;
import com.settlement.model.NetDebtObligation;
import com.settlement.model.ProcessedTransfer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetSettlementAndFeeEngineTest {

    @Test
    @DisplayName("Should apply tier 1 rate (<10,000) and tier 2 rate (>=10,000)")
    void testFeeTiers() {
        FeeReconciliationEngine feeEngine = new FeeReconciliationEngine();

        // Amount = 1,000. Fixed = 2.50, Pct = 1000 * 0.005 = 5.00, Total = 7.50
        FeeBreakdown tier1 = feeEngine.calculateFee(new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("2.50"), tier1.fixedFee());
        assertEquals(new BigDecimal("5.00"), tier1.percentageFee());
        assertEquals(new BigDecimal("7.50"), tier1.totalFee());

        // Amount = 20,000. Fixed = 2.50, Pct = 20000 * 0.0025 = 50.00, Total = 52.50
        FeeBreakdown tier2 = feeEngine.calculateFee(new BigDecimal("20000.00"));
        assertEquals(new BigDecimal("2.50"), tier2.fixedFee());
        assertEquals(new BigDecimal("50.00"), tier2.percentageFee());
        assertEquals(new BigDecimal("52.50"), tier2.totalFee());
    }

    @Test
    @DisplayName("Should perform pairwise netting per currency pair accurately")
    void testPerCurrencyPairNetting() {
        PerCurrencyNetSettlementEngine nettingEngine = new PerCurrencyNetSettlementEngine();
        FeeBreakdown dummyFee = new FeeBreakdown(new BigDecimal("2.50"), new BigDecimal("5.00"), new BigDecimal("7.50"));

        // ACC-A sends 1000 USD to ACC-B
        ProcessedTransfer t1 = new ProcessedTransfer("T1", "ACC-A", "ACC-B", new BigDecimal("1000"), "USD", new BigDecimal("1000.00"), "USD", BigDecimal.ONE, dummyFee, Instant.now());
        // ACC-B sends 400 USD to ACC-A
        ProcessedTransfer t2 = new ProcessedTransfer("T2", "ACC-B", "ACC-A", new BigDecimal("400"), "USD", new BigDecimal("400.00"), "USD", BigDecimal.ONE, dummyFee, Instant.now());
        // ACC-A sends 500 EUR to ACC-B
        ProcessedTransfer t3 = new ProcessedTransfer("T3", "ACC-A", "ACC-B", new BigDecimal("500"), "EUR", new BigDecimal("500.00"), "EUR", BigDecimal.ONE, dummyFee, Instant.now());

        List<NetDebtObligation> netDebts = nettingEngine.calculateNetSettlements(List.of(t1, t2, t3));

        assertEquals(2, netDebts.size());

        // USD Net Debt: ACC-A owes ACC-B 600.00 USD (1000 - 400 = 600)
        NetDebtObligation usdObligation = netDebts.stream().filter(d -> d.currency().equals("USD")).findFirst().orElseThrow();
        assertEquals("ACC-A", usdObligation.debtorAccountId());
        assertEquals("ACC-B", usdObligation.creditorAccountId());
        assertEquals(new BigDecimal("600.00"), usdObligation.netAmount());

        // EUR Net Debt: ACC-A owes ACC-B 500.00 EUR
        NetDebtObligation eurObligation = netDebts.stream().filter(d -> d.currency().equals("EUR")).findFirst().orElseThrow();
        assertEquals("ACC-A", eurObligation.debtorAccountId());
        assertEquals("ACC-B", eurObligation.creditorAccountId());
        assertEquals(new BigDecimal("500.00"), eurObligation.netAmount());
    }
}

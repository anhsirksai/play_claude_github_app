package com.settlement.service;

import com.settlement.model.FeeBreakdown;
import com.settlement.model.ProcessedTransfer;
import com.settlement.model.TransferRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SettlementOrchestrator {

    private final AccountSafetyValidator safetyValidator;
    private final FXRateService fxRateService;
    private final FeeReconciliationEngine feeEngine;
    private final List<ProcessedTransfer> transferHistory = Collections.synchronizedList(new ArrayList<>());

    public SettlementOrchestrator(
            AccountSafetyValidator safetyValidator,
            FXRateService fxRateService,
            FeeReconciliationEngine feeEngine) {
        this.safetyValidator = safetyValidator;
        this.fxRateService = fxRateService;
        this.feeEngine = feeEngine;
    }

    public ProcessedTransfer processTransfer(TransferRequest request) {
        safetyValidator.validateTransferSafety(request);

        BigDecimal fxRate = fxRateService.getOptimalFXRate(
                request.sourceCurrency(),
                request.targetCurrency(),
                request.timestamp()
        );

        BigDecimal targetAmount = request.sendAmount().multiply(fxRate).setScale(2, RoundingMode.HALF_UP);
        FeeBreakdown feeBreakdown = feeEngine.calculateFee(request.sendAmount());

        ProcessedTransfer processed = new ProcessedTransfer(
                request.id(),
                request.sourceAccountId(),
                request.targetAccountId(),
                request.sendAmount(),
                request.sourceCurrency(),
                targetAmount,
                request.targetCurrency(),
                fxRate,
                feeBreakdown,
                request.timestamp()
        );

        transferHistory.add(processed);
        return processed;
    }

    public List<ProcessedTransfer> getTransferHistory() {
        return List.copyOf(transferHistory);
    }
}

package com.settlement.controller;

import com.settlement.model.NetDebtObligation;
import com.settlement.model.ProcessedTransfer;
import com.settlement.model.TransferRequest;
import com.settlement.service.PerCurrencyNetSettlementEngine;
import com.settlement.service.SettlementOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementOrchestrator orchestrator;
    private final PerCurrencyNetSettlementEngine nettingEngine;

    public SettlementController(
            SettlementOrchestrator orchestrator,
            PerCurrencyNetSettlementEngine nettingEngine) {
        this.orchestrator = orchestrator;
        this.nettingEngine = nettingEngine;
    }

    @PostMapping("/transfers")
    public ResponseEntity<ProcessedTransfer> executeTransfer(@RequestBody TransferRequest request) {
        ProcessedTransfer processed = orchestrator.processTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(processed);
    }

    @GetMapping("/transfers")
    public ResponseEntity<List<ProcessedTransfer>> getAllTransfers() {
        return ResponseEntity.ok(orchestrator.getTransferHistory());
    }

    @GetMapping("/net-debts")
    public ResponseEntity<List<NetDebtObligation>> calculateNetDebts() {
        List<ProcessedTransfer> transfers = orchestrator.getTransferHistory();
        List<NetDebtObligation> netDebts = nettingEngine.calculateNetSettlements(transfers);
        return ResponseEntity.ok(netDebts);
    }
}

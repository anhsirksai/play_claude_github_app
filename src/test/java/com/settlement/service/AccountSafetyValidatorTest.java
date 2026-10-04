package com.settlement.service;

import com.settlement.exception.DomainValidationException;
import com.settlement.model.Account;
import com.settlement.model.AccountStatus;
import com.settlement.model.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccountSafetyValidatorTest {

    private AccountSafetyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccountSafetyValidator();
        validator.registerAccount(new Account("ACC-US-1", "US Sender", "USA", "USD", AccountStatus.ACTIVE));
        validator.registerAccount(new Account("ACC-SG-1", "SG Receiver", "SGP", "SGD", AccountStatus.ACTIVE));
        validator.registerAccount(new Account("ACC-BLOCKED", "Blocked Account", "USA", "USD", AccountStatus.BLOCKED));
        validator.registerAccount(new Account("ACC-SANCTIONED", "Sanctioned Account", "PRK", "KPW", AccountStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should pass validation for valid active transfer accounts")
    void testValidTransfer() {
        TransferRequest req = new TransferRequest("TR-1", "ACC-US-1", "ACC-SG-1", new BigDecimal("100.00"), "USD", "SGD", Instant.now());
        assertDoesNotThrow(() -> validator.validateTransferSafety(req));
    }

    @Test
    @DisplayName("Should reject self-transfers")
    void testSelfTransfer() {
        TransferRequest req = new TransferRequest("TR-2", "ACC-US-1", "ACC-US-1", new BigDecimal("100.00"), "USD", "USD", Instant.now());
        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> validator.validateTransferSafety(req));
        assertTrue(ex.getMessage().contains("Self-transfers are prohibited"));
    }

    @Test
    @DisplayName("Should reject non-active accounts")
    void testInactiveAccount() {
        TransferRequest req = new TransferRequest("TR-3", "ACC-BLOCKED", "ACC-SG-1", new BigDecimal("100.00"), "USD", "SGD", Instant.now());
        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> validator.validateTransferSafety(req));
        assertTrue(ex.getMessage().contains("not ACTIVE"));
    }

    @Test
    @DisplayName("Should reject sanctioned jurisdictions")
    void testSanctionedJurisdiction() {
        TransferRequest req = new TransferRequest("TR-4", "ACC-US-1", "ACC-SANCTIONED", new BigDecimal("100.00"), "USD", "KPW", Instant.now());
        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> validator.validateTransferSafety(req));
        assertTrue(ex.getMessage().contains("sanctions"));
    }
}

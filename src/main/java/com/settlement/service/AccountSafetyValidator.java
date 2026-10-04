package com.settlement.service;

import com.settlement.exception.DomainValidationException;
import com.settlement.model.Account;
import com.settlement.model.AccountStatus;
import com.settlement.model.TransferRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountSafetyValidator {

    private final Map<String, Account> accountRepository = new ConcurrentHashMap<>();
    private final Set<String> sanctionedJurisdictions = Set.of("PRK", "IRN", "SYR");

    public void registerAccount(Account account) {
        accountRepository.put(account.id(), account);
    }

    public Account getAccount(String accountId) {
        Account account = accountRepository.get(accountId);
        if (account == null) {
            throw new DomainValidationException("Account not found: " + accountId);
        }
        return account;
    }

    public void validateTransferSafety(TransferRequest request) {
        if (request.sendAmount() == null || request.sendAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("Transfer amount must be strictly greater than zero");
        }

        if (request.sourceAccountId().equalsIgnoreCase(request.targetAccountId())) {
            throw new DomainValidationException("Self-transfers are prohibited between identical accounts: " + request.sourceAccountId());
        }

        Account source = getAccount(request.sourceAccountId());
        Account target = getAccount(request.targetAccountId());

        if (source.status() != AccountStatus.ACTIVE) {
            throw new DomainValidationException("Source account " + source.id() + " is not ACTIVE. Current status: " + source.status());
        }

        if (target.status() != AccountStatus.ACTIVE) {
            throw new DomainValidationException("Target account " + target.id() + " is not ACTIVE. Current status: " + target.status());
        }

        if (sanctionedJurisdictions.contains(source.countryCode().toUpperCase())) {
            throw new DomainValidationException("Source jurisdiction blocked under compliance sanctions: " + source.countryCode());
        }

        if (sanctionedJurisdictions.contains(target.countryCode().toUpperCase())) {
            throw new DomainValidationException("Target jurisdiction blocked under compliance sanctions: " + target.countryCode());
        }
    }
}

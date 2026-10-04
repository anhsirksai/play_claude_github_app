package com.settlement.controller;

import com.settlement.model.Account;
import com.settlement.service.AccountSafetyValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountSafetyValidator safetyValidator;

    public AccountController(AccountSafetyValidator safetyValidator) {
        this.safetyValidator = safetyValidator;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        safetyValidator.registerAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(safetyValidator.getAccount(id));
    }
}

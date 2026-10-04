# AGENTS.md — Guidelines for AI Coding Agents & Human Developers

## 1. Project Overview & Core Domain
- System: Cross-Border Account Settlement & Fee Reconciliation Engine.
- Technology: Java 21, Spring Boot 3.2, Maven.
- Core Responsibility: Process international transfers, enforce account safety mapping, apply 10-minute FX price protection, reconcile tiered cross-border fees, and calculate per-currency pair net debt settlements.

## 2. Dev vs Test Workflow Separation
- **Dev Sessions**: Focus on domain models, interface abstractions, validation chains, and business logic services (`src/main/java`).
- **Test Sessions**: Focus on test suite implementation (`src/test/java`), edge cases, mock rate series, corner cases (zero amounts, negative balances, 10-minute exact boundary conditions).

## 3. Financial & Coding Standards
- **Currency & Precision**: ALWAYS use `java.math.BigDecimal` with `RoundingMode.HALF_UP` and explicit scale (2 decimal places for standard fiat, 4-6 for FX rates).
- **Immutability**: Domain events, transfer records, and settlement summaries should be immutable records or read-only DTOs.
- **Fail-Fast Safety**: Invalid account mappings, blocked jurisdictions, or unmapped currency corridors must throw typed domain exceptions immediately before transfer execution.

## 4. Build, Test & Verification Commands
- Build & Run Unit Tests: `mvn clean test`
- Run Specific Test: `mvn test -Dtest=FXRateServiceTest`
- Build Package: `mvn package -DskipTests=false`

## 5. Agent Prompting & Guidance Rules
- Never modify existing test cases unless explicitly asked; fix the implementation to satisfy tests.
- Verify every change by running `mvn test` after editing source code.

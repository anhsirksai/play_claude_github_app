# INTERVIEW_STRATEGY.md: Cross-Border Settlement Engine Architecture & Agentic Interview Blueprint

## 1. System Architecture & LLD Diagram

```
+-----------------------------------------------------------------------------------+
|                                  REST CONTROLLER                                  |
|   /api/v1/transfers  |  /api/v1/rates  |  /api/v1/settlements  |  /api/v1/accounts |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                            ACCOUNT SAFETY VALIDATOR                               |
|   - Active Account Status  - Non-Self Transfer Check  - Jurisdiction Corridor     |
|   - Currency Compatibility - Sanctions/Blacklist Rule                             |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                           10-MIN FX RATE ENGINE                                   |
|   - Lock Initial FX Rate at Timestamp T                                           |
|   - Search Window [T, T + 10 mins] for Lowest Favorable Rate for User            |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                         FEE RECONCILIATION ENGINE                                 |
|   - Fixed Cross-Border Fee Calculation                                            |
|   - Tiered Percentage Fee Calculation                                             |
|   - Sender vs Receiver Fee Allocation                                             |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                      PER-CURRENCY PAIR NET SETTLEMENT ENGINE                      |
|   - Aggregate Debt Ledgers grouped by (SourceAccount, TargetAccount, Currency)    |
|   - Pairwise Multilateral Netting per Currency Pair                               |
|   - Output Final Obligation Ledger                                                |
+-----------------------------------------------------------------------------------+
```

---

## 2. Key Domain Requirements & Algorithmic Design

### A. 10-Minute FX Rate Guarantee / Price Protection
- **Requirement**: When a transfer is initiated at timestamp `T`, an initial rate $R_{initial}$ is quoted. If a lower/more favorable exchange rate $R_{min}$ occurs within $[T, T + 10\text{ minutes}]$, $R_{min}$ is applied.
- **Algorithm**:
  $$\text{Effective Rate} = \min_{t \in [T, T + 10\text{ min}]} \{ \text{Rate}(t) \}$$
  *(User pays the lowest rate captured in the 10-minute window)*.

### B. Account Mapping Safety Engine
- Checks performed sequentially:
  1. `AccountActiveValidator`: Ensure source and destination accounts are `ACTIVE`.
  2. `DistinctAccountValidator`: Prevent `sourceAccountId == targetAccountId`.
  3. `SupportedCorridorValidator`: Check if transfer corridor (e.g., `US -> SG`) and currency pair (e.g., `USD/SGD`) is permitted.
  4. `SanctionsBlacklistValidator`: Enforce compliance rules for restricted accounts/jurisdictions.

### C. Fee Reconciliation Engine
- Calculates:
  - Base Fee: Fixed transfer fee ($F_{fixed}$).
  - Tiered Percentage Fee: $F_{pct} = \text{Amount} \times \text{TierRate}$.
  - Total Transfer Cost = $\text{Converted Amount} + F_{fixed} + F_{pct}$.

### D. Per-Currency Pair Net Debt Settlement Engine
- Groups transfers by `Currency`.
- Builds a directed net balance matrix $B(A, B)$ representing Net Amount $A \to B$.
- For each account pair $(A, B)$ in currency $C$:
  $$\text{Net Obligation}(A \to B) = \sum \text{Transfers}(A \to B) - \sum \text{Transfers}(B \to A)$$
- If $\text{Net Obligation}(A \to B) > 0$, $A$ owes $B$. If $< 0$, $B$ owes $A$.

---

## 3. Interview Strategy Guide for Agentic Coding (Jules, Cursor, Claude Code)

### Phase 1: Clarification & Domain Framing (0–10 mins)
- State goals clearly to the interviewer.
- Ask clarifying questions regarding edge cases, rate locking rules, and netting scope.
- Establish `AGENTS.md` in the repository root to give clear instructions to your AI agent.

### Phase 2: Agent Guidance & Prompting Strategy (10–25 mins)
- **Give High-Level Specifications**: Provide structured requirements and let the agent generate boilerplates and algorithms.
- **Review Critically**: Do not accept agent code blindly. Inspect domain models for correctness (e.g., `BigDecimal` usage, immutable records, proper validation exceptions).
- **Sub-agent / Session Separation**: Keep dev logic implementation and unit test suites modular.

### Phase 3: Verification & Edge Case Testing (25–45 mins)
- Instruct the agent to write unit tests for:
  - Exact 10-minute boundary condition for FX rates.
  - Pairwise netting balance cancellation ($A \to B$ vs $B \to A$).
  - Safety validation violations.
- Run tests via terminal (`mvn test`) to prove correctness to the interviewer.

### Phase 4: Architecture Review & Code Walkthrough (45–60 mins)
- Explain design patterns used (Strategy pattern for fees/safety checks, Service layer separation).
- Highlight how `AGENTS.md` guided the agent cleanly without human intervention loop stalls.

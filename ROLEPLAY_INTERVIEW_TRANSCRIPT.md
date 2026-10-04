# ROLEPLAY_INTERVIEW_TRANSCRIPT.md: AI Agentic Coding Interview Simulation

**Setting**: Live 60-Minute System Design & Machine Coding Interview
**Candidate**: Candidate (You)
**Interviewer**: Senior Tech Lead (Interviewer)
**Tools Allowed**: Jules / Cursor / Claude Code (Browser-based & Terminal Agentic Coding Assistants)

---

### [00:00 - 05:00] Phase 1: Requirement Gathering & Goal Clarification

**Interviewer**: Welcome! Today we are building the core engine for a **Cross-Border Account Settlement & Fee Reconciliation Engine**. You have access to Jules and agentic tools. How do you plan to approach this?

**Candidate**: Thanks! Before jumping into code, I want to clarify all core functional requirements to make sure we build the right system:
1. **FX Rate Protection**: You mentioned users shouldn't be affected by currency rate fluctuations after initiating a transfer. Specifically, if a lower FX rate becomes available within a 10-minute window, the user gets that lower rate. Is that window strict `[T, T + 10m]`?
2. **Account Safety**: We need safety mapping checks—what validations are expected? (Active accounts, self-transfer blocking, sanctions checks).
3. **Netting**: Is netting performed per currency pair or across all currencies?
4. **Agent Usage**: I will configure an `AGENTS.md` file in the repo root so my coding agent understands the architecture conventions, financial precision rules (`BigDecimal`), and testing expectations.

**Interviewer**: Excellent questions. Yes: 10-minute FX price protection window, per-currency pair netting, and strict safety validation. Using `AGENTS.md` is a great practice. Proceed!

---

### [05:00 - 15:00] Phase 2: System Architecture & AGENTS.md Setup

**Candidate**: I will now write `AGENTS.md` to define explicit boundaries for Jules/AI agents:
- Tech Stack: Java 21, Spring Boot 3.2, Maven.
- Precision: Strict `BigDecimal` with `RoundingMode.HALF_UP`.
- Workflow: Separate Dev and Test sessions. Run `mvn clean test` to verify every change.

*(Candidate writes `AGENTS.md` and generates architectural diagram in `INTERVIEW_STRATEGY.md`)*

**Candidate**: Let's review the architectural diagram. Transfers pass through:
1. `AccountSafetyValidator` (Sanctions, status, self-transfers)
2. `FXRateService` (Looks up historical window $[T, T+10\text{min}]$ for optimal rate)
3. `FeeReconciliationEngine` (Tiered fixed + percentage fees)
4. `PerCurrencyNetSettlementEngine` (Aggregates directional debt pairs by currency).

**Interviewer**: The architecture is clear and modular. Let's start implementing.

---

### [15:00 - 35:00] Phase 3: Agentic Prompting, Coding & Critical Review

**Candidate**: I will instruct Jules to implement the domain models, services, and REST controllers.

*(Candidate prompts agent with clear domain specs)*

**Candidate (Reviewing Agent Output)**:
Let's inspect what Jules generated:
- In `FXRateService.java`, Jules used `ConcurrentHashMap` for rate quotes and implemented the 10-minute minimum rate filter:
  ```java
  Optional<BigDecimal> minRate = quotes.stream()
      .filter(q -> !q.timestamp().isBefore(requestTime) && !q.timestamp().isAfter(windowEnd))
      .map(FXRateQuote::rate)
      .min(Comparator.naturalOrder());
  ```
- In `PerCurrencyNetSettlementEngine.java`, canonical account pair keys (`acc1:acc2`) are used to prevent duplicate opposite entries ($A \to B$ vs $B \to A$).

**Interviewer**: Good catch reviewing the key normalization in netting. How do you handle cases where no rate quote exists in the future window?

**Candidate**: The fallback logic checks for the most recent rate quote recorded prior to `requestTime`. If none exists at all, it throws a domain exception (`IllegalStateException`) which gets handled by our `@RestControllerAdvice`.

---

### [35:00 - 50:00] Phase 4: Testing, Verification & Debugging

**Candidate**: Now let's verify everything using unit tests. I will run the test suite via the terminal:

```bash
$ mvn clean test
[INFO] Running com.settlement.service.FXRateServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0
[INFO] Running com.settlement.service.AccountSafetyValidatorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0
[INFO] Running com.settlement.service.NetSettlementAndFeeEngineTest
[INFO] Tests run: 2, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS (10 tests run, 0 failures)
```

**Candidate**: All 10 tests passed cleanly. We verified:
1. Exact 10-minute boundary lookups for FX rates.
2. Rejection of self-transfers, blocked accounts, and sanctioned jurisdictions (e.g., North Korea).
3. Tier 1 vs Tier 2 fee calculations.
4. Multilateral pairwise net balance calculations per currency.

---

### [50:00 - 60:00] Phase 5: Wrap-up & Lessons Learned

**Interviewer**: Outstanding demonstration! You used the AI agent as a high-speed accelerator while maintaining critical oversight, verifying code, and enforcing testing discipline.

**Key Takeaways for Candidate**:
1. **Never accept agent code blindly**: Review domain logic, scale/rounding, and edge cases.
2. **Use AGENTS.md for steering**: Setting rules up front prevents AI hallucination or unwanted refactoring.
3. **Verify with automated tests**: Running `mvn test` proves correctness without manual trial and error.

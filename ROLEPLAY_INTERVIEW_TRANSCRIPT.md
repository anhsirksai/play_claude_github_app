# ROLEPLAY_INTERVIEW_TRANSCRIPT.md: AI Agentic Coding Interview Simulation & Exact Prompt Log

**Setting**: Live 60-Minute System Design & Machine Coding Interview
**Candidate**: Candidate (You)
**Interviewer**: Senior Tech Lead (Interviewer)
**Tools Allowed**: Jules / Cursor / Claude Code (Browser-based & Terminal Agentic Coding Assistants)

---

### [00:00 - 05:00] Phase 1: Requirement Gathering & Goal Clarification

**Interviewer**: Welcome! Today we are building the core engine for a **Cross-Border Account Settlement & Fee Reconciliation Engine**. You have access to Jules and agentic tools. How do you plan to approach this?

**Candidate**: Thanks! Before jumping into code, I want to clarify all core functional requirements:
1. **FX Rate Protection**: You mentioned users shouldn't be affected by currency rate fluctuations after initiating a transfer. Specifically, if a lower FX rate becomes available within a 10-minute window, the user gets that lower rate. Is that window strict `[T, T + 10m]`?
2. **Account Safety**: We need safety mapping checks—what validations are expected? (Active accounts, self-transfer blocking, sanctions checks).
3. **Netting**: Is netting performed per currency pair or across all currencies?
4. **Agent Usage**: I will configure an `AGENTS.md` file in the repo root so my coding agent understands the architecture conventions, financial precision rules (`BigDecimal`), and testing expectations.

**Interviewer**: Excellent questions. Yes: 10-minute FX price protection window, per-currency pair netting, and strict safety validation. Using `AGENTS.md` is a great practice. Proceed!

---

### [05:00 - 15:00] Phase 2: Agent Sessions & Exact Prompts Log

Below are the exact, raw human prompts (crude/realistic as given during fast-paced interview settings) across separate agent sessions:

#### 🟢 SESSION 1: Core Domain Logic & REST Endpoints (Dev Session)

> **Candidate Prompt to Agent (Raw/Crude)**:
> *"hey build the spring boot 3 cross border settlement engine in java 21. make models for Account, TransferRequest, FXRateQuote, ProcessedTransfer, FeeBreakdown, NetDebtObligation. for fx rate, if user transfers at T, find the lowest rate in next 10 mins [T, T+10m] and use that. if same currency return 1. safety check: block self transfer, check account is ACTIVE, block sanctioned countries like PRK, IRN, SYR. fees: flat 2.50 plus 0.5% below 10k or 0.25% above 10k. netting: do pairwise net debt per currency pair. make rest controllers too for accounts, rates, transfers, net-debts."*

**Candidate (Reviewing Session 1 Output)**:
"Let's inspect what Jules generated from this prompt:
- In `FXRateService.java`, the 10-minute lower rate selection logic uses stream `.filter()` and `.min()`:
  ```java
  Optional<BigDecimal> minRate = quotes.stream()
      .filter(q -> !q.timestamp().isBefore(requestTime) && !q.timestamp().isAfter(windowEnd))
      .map(FXRateQuote::rate)
      .min(Comparator.naturalOrder());
  ```
- In `PerCurrencyNetSettlementEngine.java`, canonical account pair keys (`acc1:acc2`) are used so opposing transfers ($A \to B$ vs $B \to A$) cancel each other out correctly.
- All amounts use `BigDecimal` with scale and `RoundingMode.HALF_UP` as specified in `AGENTS.md`."

---

#### 🧪 SESSION 2: Testing & Edge Cases (Test Session)

> **Candidate Prompt to Agent (Raw/Crude)**:
> *"open test session now. write unit tests for FXRateService, AccountSafetyValidator, and NetSettlementAndFeeEngine. test cases needed: same currency rate = 1, lowest rate selection inside 10 min window (add quotes at T, T+3m lower, T+7m higher, T+15m out of window), self transfer fails, inactive account fails, sanctioned country PRK fails, fee tiers below and above 10k, and per currency pairwise netting where A sends 1000 USD to B and B sends 400 USD to A (net should be A owes B 600 USD). run mvn test and make sure everything passes."*

**Candidate (Reviewing Session 2 Output)**:
"Jules created `FXRateServiceTest`, `AccountSafetyValidatorTest`, and `NetSettlementAndFeeEngineTest` covering all requested scenario matrices. Running `mvn test` shows 10/10 tests passed in 5.3s."

---

#### 🔧 SESSION 3: Exception Handling & Edge Refinement (Refinement Session)

> **Candidate Prompt to Agent (Raw/Crude)**:
> *"add GlobalExceptionHandler for DomainValidationException (returns 400) and IllegalStateException (returns 422). make sure controller responses return proper json error body with timestamp and message. run mvn test again."*

**Candidate (Reviewing Session 3 Output)**:
"The exception handler was added smoothly and all unit/integration tests remain 100% green."

---

### [35:00 - 50:00] Phase 3: Terminal Verification & Test Execution

**Candidate**: Let's run the full test suite in the terminal to verify build stability:

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

---

### [50:00 - 60:00] Phase 4: Interviewer Q&A & Key Takeaways

**Interviewer**: Excellent! The inclusion of exact prompt logs across separated sessions shows a clear strategy for directing AI agents under interview time pressure.

**Key Prompting Strategies for AI-Assisted Interviews**:
1. **Crude Prompts Work with AGENTS.md**: You don't need perfect grammar when `AGENTS.md` already defines coding standards, packages, and rounding rules.
2. **Session Isolation**: Keep Dev and Test prompts in separate sessions or logical steps so the agent doesn't write mock tests that pass flawed implementation logic.
3. **Explicit Edge Cases**: List explicit numbers in your test prompts (e.g., "A sends 1000, B sends 400, net = 600") so the agent builds exact assertions.

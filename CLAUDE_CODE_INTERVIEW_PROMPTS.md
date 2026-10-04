# CLAUDE_CODE_INTERVIEW_PROMPTS.md: Step-by-Step Interviewee Prompting Guide

This guide provides **exact, battle-tested candidate prompts** for every phase of a 60-minute agentic machine coding interview.

Unlike multi-instance web agents (like Jules) that run separate isolated sandboxes per session, local CLI tools like **Claude Code** run directly in your local repository workspace on your Mac. This guide includes explicit context-isolation techniques to prevent code stomping or context drift.

---

## 🚀 Pre-Interview Setup (00:00 - 05:00)

### Step 0: Create `AGENTS.md` (Local Guardrails)
Run this prompt first to establish ground rules in your workspace before Claude Code generates any code.

```text
Create AGENTS.md in repo root.
Keep it under 6 bullet points:
- Java 21, Spring Boot 3.2, Maven
- Always use java.math.BigDecimal with HALF_UP rounding for money/rates
- Never modify existing tests without approval
- Always verify changes by running `mvn test` in bash
- Fail-fast on domain errors with typed exceptions
```

---

## 🛠️ PHASE 1: Domain & Core Logic Implementation (10:00 - 25:00)

### Session 1 Strategy (Claude Code Single Repo Context)
*Objective*: Build the domain core in `src/main/java`. Instruct Claude Code explicitly to touch **only source code** and leave `src/test/java` untouched for now.

```text
Build the core domain for the Cross-Border Settlement Engine in src/main/java:
1. Models: Account (id, name, countryCode, currency, status), FXRateQuote, TransferRequest, ProcessedTransfer, FeeBreakdown, NetDebtObligation.
2. FXRateService: Look up rates for pair (fromCurrency, toCurrency). If transfer timestamp is T, pick the MINIMUM rate recorded in window [T, T + 10 mins]. Return 1.0 if same currency.
3. AccountSafetyValidator: Throw DomainValidationException if source == target account, if source/target is not ACTIVE, or if country is in sanctioned list ["PRK", "IRN", "SYR"].
4. FeeReconciliationEngine: Fixed fee = 2.50. Tier 1 = 0.5% if amount < 10,000; Tier 2 = 0.25% if amount >= 10,000.
5. PerCurrencyNetSettlementEngine: Pairwise net settlement per target currency. Cancel out opposing transfers between account pairs (A -> B vs B -> A).
6. REST Controllers: /api/v1/accounts, /api/v1/rates, /api/v1/settlements.

Do NOT edit any files in src/test/java yet. Only create src/main/java code.
```

---

## 🧪 PHASE 2: Test Suite & Edge Case Verification (25:00 - 40:00)

### Session 2 Strategy (Claude Code Single Repo Context)
*Objective*: Add comprehensive tests under `src/test/java`. Tell Claude Code to write tests that challenge the implementation without touching the main business logic unless a test fails.

```text
Now switch to test writing mode. Create unit tests under src/test/java:
1. FXRateServiceTest:
   - Test same currency returns 1.0.
   - Test 10-min window rate selection: add quote at T (0.92), T+3m (0.9150 - lowest!), T+7m (0.93), T+15m (0.90 - outside window). Assert selected rate is 0.9150.
2. AccountSafetyValidatorTest:
   - Test valid transfer passes.
   - Test self-transfer throws DomainValidationException.
   - Test inactive account throws DomainValidationException.
   - Test sanctioned country (PRK) throws DomainValidationException.
3. NetSettlementAndFeeEngineTest:
   - Test fee tiers (<10k vs >=10k).
   - Test per-currency pair netting: ACC-A sends 1000 USD to ACC-B, ACC-B sends 400 USD to ACC-A. Assert net debt is ACC-A owes ACC-B 600 USD.

After generating tests, execute `mvn test` using bash and report the output.
```

---

## ⚡ PHASE 3: Refinement & Edge Case Hardening (40:00 - 50:00)

### Session 3 Strategy (Claude Code Single Repo Context)
*Objective*: Refine exception handling and edge case responses while maintaining passing tests.

```text
Add GlobalExceptionHandler in src/main/java/com/settlement/exception/GlobalExceptionHandler.java:
- Catch DomainValidationException -> return 400 BAD_REQUEST with JSON body {timestamp, status, error, message}.
- Catch IllegalStateException -> return 422 UNPROCESSABLE_ENTITY.

Run `mvn test` in bash to confirm no regressions.
```

---

## 💡 Top 5 Tips for Using Claude Code in Live Interviews

1. **Explicit File Boundaries**: Always specify `src/main/java` or `src/test/java` in prompts to prevent Claude Code from modifying tests while fixing source code or vice versa.
2. **Use Terminal Verification**: Tell Claude Code to run build/test commands (`mvn test`) directly so you can see live test outputs without leaving your CLI session.
3. **Use Git Safety Check**: In a single-repo CLI environment, run `git status` or `git diff` periodically to inspect what files Claude Code touched before moving to the next question.
4. **Crude Prompts are Okay**: Under time pressure, bulleted lists with exact math numbers (e.g., "1000 - 400 = 600 net debt") work better than long descriptive paragraphs.
5. **Steer via Small Increments**: If Claude Code makes a wrong architectural assumption, immediately hit `Ctrl+C` or send a corrective prompt: *"Stop. Do not change the controller interface. Fix only the service method."*

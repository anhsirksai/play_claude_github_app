package com.settlement.service;

import com.settlement.model.NetDebtObligation;
import com.settlement.model.ProcessedTransfer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PerCurrencyNetSettlementEngine {

    /**
     * Aggregates a sequence of processed cross-border transfers and calculates
     * net debt obligations per currency pair.
     */
    public List<NetDebtObligation> calculateNetSettlements(List<ProcessedTransfer> transfers) {
        // Map key: Currency -> Map key: AccountPairKey -> Net Balance (Positive means AccountA owes AccountB)
        Map<String, Map<String, BigDecimal>> netLedgersByCurrency = new HashMap<>();

        for (ProcessedTransfer transfer : transfers) {
            String currency = transfer.targetCurrency();
            String accA = transfer.sourceAccountId();
            String accB = transfer.targetAccountId();
            BigDecimal amount = transfer.targetAmount();

            netLedgersByCurrency.putIfAbsent(currency, new HashMap<>());
            Map<String, BigDecimal> pairLedger = netLedgersByCurrency.get(currency);

            String pairKey = buildPairKey(accA, accB);
            boolean isCanonicalOrder = accA.compareTo(accB) < 0;

            BigDecimal currentBalance = pairLedger.getOrDefault(pairKey, BigDecimal.ZERO);
            if (isCanonicalOrder) {
                // accA owes accB
                currentBalance = currentBalance.add(amount);
            } else {
                // accB owes accA (subtract)
                currentBalance = currentBalance.subtract(amount);
            }
            pairLedger.put(pairKey, currentBalance);
        }

        List<NetDebtObligation> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, BigDecimal>> currencyEntry : netLedgersByCurrency.entrySet()) {
            String currency = currencyEntry.getKey();
            for (Map.Entry<String, BigDecimal> pairEntry : currencyEntry.getValue().entrySet()) {
                BigDecimal balance = pairEntry.getValue().setScale(2, RoundingMode.HALF_UP);
                if (balance.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                String[] accounts = pairEntry.getKey().split(":");
                String firstAccount = accounts[0];
                String secondAccount = accounts[1];

                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    // firstAccount owes secondAccount
                    result.add(new NetDebtObligation(firstAccount, secondAccount, currency, balance));
                } else {
                    // secondAccount owes firstAccount
                    result.add(new NetDebtObligation(secondAccount, firstAccount, currency, balance.abs()));
                }
            }
        }

        return result;
    }

    private String buildPairKey(String acc1, String acc2) {
        if (acc1.compareTo(acc2) < 0) {
            return acc1 + ":" + acc2;
        } else {
            return acc2 + ":" + acc1;
        }
    }
}

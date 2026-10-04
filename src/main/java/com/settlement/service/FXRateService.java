package com.settlement.service;

import com.settlement.model.FXRateQuote;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FXRateService {

    private final Map<String, List<FXRateQuote>> rateHistory = new ConcurrentHashMap<>();

    private String getPairKey(String from, String to) {
        return from.toUpperCase() + "_" + to.toUpperCase();
    }

    public void addRateQuote(FXRateQuote quote) {
        String key = getPairKey(quote.fromCurrency(), quote.toCurrency());
        rateHistory.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>())).add(quote);
    }

    /**
     * Calculates the optimal FX rate for a transfer requested at timestamp `requestTime`.
     * Applies the lowest rate recorded within the 10-minute window [requestTime, requestTime + 10 mins].
     * If source and target currencies are identical, returns 1.0000.
     */
    public BigDecimal getOptimalFXRate(String fromCurrency, String toCurrency, Instant requestTime) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        String key = getPairKey(fromCurrency, toCurrency);
        List<FXRateQuote> quotes = rateHistory.getOrDefault(key, List.of());

        Instant windowEnd = requestTime.plus(Duration.ofMinutes(10));

        Optional<BigDecimal> minRate = quotes.stream()
            .filter(q -> !q.timestamp().isBefore(requestTime) && !q.timestamp().isAfter(windowEnd))
            .map(FXRateQuote::rate)
            .min(Comparator.naturalOrder());

        if (minRate.isPresent()) {
            return minRate.get();
        }

        // Fallback: look for the most recent rate prior to or equal to requestTime
        Optional<FXRateQuote> latestPriorQuote = quotes.stream()
            .filter(q -> !q.timestamp().isAfter(requestTime))
            .max(Comparator.comparing(FXRateQuote::timestamp));

        return latestPriorQuote
            .map(FXRateQuote::rate)
            .orElseThrow(() -> new IllegalStateException("No FX rate quote available for currency pair: " + key));
    }
}

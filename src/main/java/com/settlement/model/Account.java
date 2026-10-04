package com.settlement.model;

public record Account(
    String id,
    String name,
    String countryCode,
    String currency,
    AccountStatus status
) {}

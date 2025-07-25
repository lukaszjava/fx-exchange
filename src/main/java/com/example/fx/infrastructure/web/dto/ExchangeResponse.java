package com.example.fx.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeResponse(
    AccountResponse.Balances balances, BigDecimal rate, Instant rateTimestamp) {}

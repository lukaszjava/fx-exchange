package com.example.fx.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
    UUID accountId, String firstName, String lastName, Balances balances) {

  public record Balances(BigDecimal PLN, BigDecimal USD) {}
}

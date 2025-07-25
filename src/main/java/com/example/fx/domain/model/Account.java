package com.example.fx.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Account(
    UUID id, String firstName, String lastName, BigDecimal pln, BigDecimal usd, long version) {

  public Account withBalances(BigDecimal newPln, BigDecimal newUsd) {
    return new Account(id, firstName, lastName, newPln, newUsd, version);
  }
}

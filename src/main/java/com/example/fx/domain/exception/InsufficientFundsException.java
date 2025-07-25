package com.example.fx.domain.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
  public InsufficientFundsException(String currency, BigDecimal requested, BigDecimal available) {
    super(
        "Not enough " + currency + " to exchange " + requested + " (available: " + available + ")");
  }
}

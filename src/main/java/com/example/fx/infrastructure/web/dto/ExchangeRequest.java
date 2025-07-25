package com.example.fx.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ExchangeRequest(@NotNull Direction direction, @DecimalMin("0.01") BigDecimal amount) {

  public enum Direction {
    PLN_TO_USD,
    USD_TO_PLN
  }
}

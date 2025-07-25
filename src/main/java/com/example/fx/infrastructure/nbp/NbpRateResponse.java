package com.example.fx.infrastructure.nbp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NbpRateResponse(String table, String currency, String code, List<Rate> rates) {
  public record Rate(LocalDate effectiveDate, BigDecimal mid) {}
}

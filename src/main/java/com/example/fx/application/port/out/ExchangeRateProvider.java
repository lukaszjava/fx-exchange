package com.example.fx.application.port.out;

import java.math.BigDecimal;

public interface ExchangeRateProvider {
  BigDecimal getPlnUsdRate();
}

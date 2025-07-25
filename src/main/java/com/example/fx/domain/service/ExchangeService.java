package com.example.fx.domain.service;

import com.example.fx.domain.exception.InsufficientFundsException;
import com.example.fx.domain.model.Account;
import com.example.fx.domain.model.Direction;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeService {

  public Account exchange(Account acc, Direction dir, BigDecimal amount, BigDecimal rate) {
    if (amount.signum() <= 0) throw new IllegalArgumentException("amount must be > 0");

    switch (dir) {
      case PLN_TO_USD -> {
        if (acc.pln().compareTo(amount) < 0) {
          throw new InsufficientFundsException("PLN", amount, acc.pln());
        }
        BigDecimal usdGain = amount.divide(rate, 4, RoundingMode.HALF_UP);
        return acc.withBalances(acc.pln().subtract(amount), acc.usd().add(usdGain));
      }
      case USD_TO_PLN -> {
        if (acc.usd().compareTo(amount) < 0) {
          throw new InsufficientFundsException("USD", amount, acc.usd());
        }
        BigDecimal plnGain = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return acc.withBalances(acc.pln().add(plnGain), acc.usd().subtract(amount));
      }
      default -> throw new IllegalStateException("Unknown direction: " + dir);
    }
  }
}

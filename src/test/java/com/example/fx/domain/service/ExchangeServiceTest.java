package com.example.fx.domain.service;

import static org.assertj.core.api.Assertions.*;

import com.example.fx.domain.exception.InsufficientFundsException;
import com.example.fx.domain.model.Account;
import com.example.fx.domain.model.Direction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExchangeServiceTest {

  private final ExchangeService service = new ExchangeService();

  private Account baseAccount() {
    return new Account(
        UUID.randomUUID(), "John", "Doe", new BigDecimal("1000.00"), new BigDecimal("50.0000"), 0);
  }

  @Nested
  @DisplayName("PLN -> USD")
  class PlnToUsd {

    @Test
    void success_rateRoundingTo4() {
      Account acc = baseAccount();
      Account updated =
          service.exchange(
              acc, Direction.PLN_TO_USD, new BigDecimal("333.33"), new BigDecimal("4.1234"));

      assertThat(updated.pln()).isEqualByComparingTo("666.67");
      assertThat(updated.usd())
          .isEqualByComparingTo(
              new BigDecimal("50.0000")
                  .add(
                      new BigDecimal("333.33")
                          .divide(new BigDecimal("4.1234"), 4, RoundingMode.HALF_UP)));
      assertThat(updated.version()).isEqualTo(acc.version());
    }

    @Test
    void insufficientPln_throws() {
      Account acc = baseAccount();
      assertThatThrownBy(
              () ->
                  service.exchange(
                      acc, Direction.PLN_TO_USD, new BigDecimal("1001.00"), new BigDecimal("4.00")))
          .isInstanceOf(InsufficientFundsException.class);
    }
  }

  @Nested
  @DisplayName("USD -> PLN")
  class UsdToPln {

    @ParameterizedTest(name = "amount={0}, rate={1}")
    @CsvSource({"10.0000,4.0000", "0.0100,3.9876", "49.9999,4.1234"})
    void success(String amountStr, String rateStr) {
      Account acc = baseAccount();
      BigDecimal amount = new BigDecimal(amountStr);
      BigDecimal rate = new BigDecimal(rateStr);

      Account updated = service.exchange(acc, Direction.USD_TO_PLN, amount, rate);

      assertThat(updated.usd()).isEqualByComparingTo(acc.usd().subtract(amount));
      assertThat(updated.pln())
          .isEqualByComparingTo(
              acc.pln().add(amount.multiply(rate).setScale(2, RoundingMode.HALF_UP)));
      assertThat(updated.version()).isEqualTo(acc.version());
    }

    @Test
    void insufficientUsd_throws() {
      Account acc = baseAccount();
      assertThatThrownBy(
              () ->
                  service.exchange(
                      acc, Direction.USD_TO_PLN, new BigDecimal("55.0000"), new BigDecimal("4.00")))
          .isInstanceOf(InsufficientFundsException.class);
    }
  }

  @Test
  void zeroOrNegativeAmount_illegalArgument() {
    Account acc = baseAccount();
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> service.exchange(acc, Direction.PLN_TO_USD, BigDecimal.ZERO, BigDecimal.ONE));
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                service.exchange(acc, Direction.PLN_TO_USD, new BigDecimal("-1"), BigDecimal.ONE));
  }
}

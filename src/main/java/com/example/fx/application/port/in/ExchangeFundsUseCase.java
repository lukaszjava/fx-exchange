package com.example.fx.application.port.in;

import com.example.fx.domain.model.Account;
import com.example.fx.domain.model.Direction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface ExchangeFundsUseCase {
  Result exchange(UUID accountId, Direction direction, BigDecimal amount);

  record Result(Account account, BigDecimal rate, Instant timestamp) {}
}

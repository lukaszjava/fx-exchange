package com.example.fx.application.service;

import com.example.fx.application.port.in.ExchangeFundsUseCase;
import com.example.fx.application.port.out.AccountRepository;
import com.example.fx.application.port.out.ExchangeRateProvider;
import com.example.fx.domain.exception.AccountNotFoundException;
import com.example.fx.domain.model.Account;
import com.example.fx.domain.model.Direction;
import com.example.fx.domain.service.ExchangeService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ExchangeFundsHandler implements ExchangeFundsUseCase {

  private final AccountRepository repository;
  private final ExchangeRateProvider rateProvider;
  private final ExchangeService exchangeService;

  public ExchangeFundsHandler(
      AccountRepository repository,
      ExchangeRateProvider rateProvider,
      ExchangeService exchangeService) {
    this.repository = repository;
    this.rateProvider = rateProvider;
    this.exchangeService = exchangeService;
  }

  @Override
  public Result exchange(UUID accountId, Direction direction, BigDecimal amount) {
    Account acc =
        repository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    BigDecimal rate = rateProvider.getPlnUsdRate();
    Account updated = exchangeService.exchange(acc, direction, amount, rate);
    repository.update(updated, acc.version());
    return new Result(updated, rate, Instant.now());
  }
}

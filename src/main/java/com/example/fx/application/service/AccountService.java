package com.example.fx.application.service;

import com.example.fx.application.port.in.CreateAccountUseCase;
import com.example.fx.application.port.in.GetAccountUseCase;
import com.example.fx.application.port.out.AccountRepository;
import com.example.fx.domain.exception.AccountNotFoundException;
import com.example.fx.domain.model.Account;
import java.math.BigDecimal;
import java.util.UUID;

public class AccountService implements CreateAccountUseCase, GetAccountUseCase {

  private final AccountRepository repository;

  public AccountService(AccountRepository repository) {
    this.repository = repository;
  }

  @Override
  public UUID create(String firstName, String lastName, BigDecimal initialPln) {
    Account account =
        new Account(UUID.randomUUID(), firstName, lastName, initialPln, BigDecimal.ZERO, 0);
    repository.save(account);
    return account.id();
  }

  @Override
  public Account get(UUID id) {
    return repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
  }
}

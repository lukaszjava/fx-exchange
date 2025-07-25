package com.example.fx.application.port.out;

import com.example.fx.domain.model.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
  Account save(Account account);

  Optional<Account> findById(UUID id);

  Account update(Account account, long expectedVersion);
}

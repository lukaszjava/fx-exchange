package com.example.fx.infrastructure.repo.jpa;

import com.example.fx.application.port.out.AccountRepository;
import com.example.fx.domain.model.Account;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RequiredArgsConstructor
public class JpaAccountRepository implements AccountRepository {

  private final SpringAccountJpaRepository repository;
  private final AccountEntityMapper entityMapper;

  @Override
  public Account save(Account account) {
    AccountEntity entity = entityMapper.toEntity(account);
    repository.save(entity);
    return account;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Account> findById(UUID id) {
    return repository.findById(id).map(entityMapper::toDomain);
  }

  @Override
  public Account update(Account account, long expectedVersion) {
    AccountEntity current = repository.findById(account.id()).orElseThrow();
    if (current.getVersion() != expectedVersion) {
      throw new org.springframework.dao.OptimisticLockingFailureException("version mismatch");
    }
    AccountEntity updated = entityMapper.toEntity(account);
    repository.save(updated);
    return account;
  }
}

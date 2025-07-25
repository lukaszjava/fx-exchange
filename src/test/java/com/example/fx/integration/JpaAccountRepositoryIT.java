package com.example.fx.integration;

import static org.assertj.core.api.Assertions.*;

import com.example.fx.application.port.out.AccountRepository;
import com.example.fx.domain.model.Account;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

@SpringBootTest
class JpaAccountRepositoryIT extends BaseIntegrationTest {

  @Autowired AccountRepository repository;

  @Test
  void save_find_update_flow() {
    UUID id = UUID.randomUUID();
    Account account = new Account(id, "John", "Doe", new BigDecimal("100.00"), BigDecimal.ZERO, 0);

    repository.save(account);

    Account loaded = repository.findById(id).orElseThrow();
    assertThat(loaded.pln()).isEqualByComparingTo("100.00");
    assertThat(loaded.version()).isEqualTo(0);

    Account updated = loaded.withBalances(new BigDecimal("90.00"), new BigDecimal("2.50"));
    repository.update(updated, loaded.version());

    Account afterUpdate = repository.findById(id).orElseThrow();
    assertThat(afterUpdate.pln()).isEqualByComparingTo("90.00");
    assertThat(afterUpdate.usd()).isEqualByComparingTo("2.50");
    assertThat(afterUpdate.version()).isEqualTo(1);
  }

  @Test
  void optimisticLocking_shouldThrow() {
    UUID id = UUID.randomUUID();
    Account account = new Account(id, "John", "Doe", BigDecimal.TEN, BigDecimal.ZERO, 0);
    repository.save(account);

    Account account0 = repository.findById(id).orElseThrow();
    Account account1 = repository.findById(id).orElseThrow();

    repository.update(
        account0.withBalances(new BigDecimal("5.00"), BigDecimal.ZERO), account0.version());

    assertThatThrownBy(
            () ->
                repository.update(
                    account1.withBalances(new BigDecimal("3.00"), BigDecimal.ZERO),
                    account1.version()))
        .isInstanceOf(OptimisticLockingFailureException.class);
  }
}

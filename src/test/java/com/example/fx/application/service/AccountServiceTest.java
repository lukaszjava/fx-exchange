package com.example.fx.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.fx.application.port.out.AccountRepository;
import com.example.fx.domain.exception.AccountNotFoundException;
import com.example.fx.domain.model.Account;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

  AccountRepository repo = mock(AccountRepository.class);
  AccountService service = new AccountService(repo);

  @Test
  void create_persists_and_returns_id() {
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    UUID id = service.create("John", "Doe", new BigDecimal("123.45"));
    assertThat(id).isNotNull();
    verify(repo).save(any(Account.class));
  }

  @Test
  void get_found() {
    UUID id = UUID.randomUUID();
    when(repo.findById(id))
        .thenReturn(Optional.of(new Account(id, "A", "B", BigDecimal.TEN, BigDecimal.ZERO, 0)));
    Account acc = service.get(id);
    assertThat(acc.id()).isEqualTo(id);
  }

  @Test
  void get_notFound_throws() {
    UUID id = UUID.randomUUID();
    when(repo.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.get(id)).isInstanceOf(AccountNotFoundException.class);
  }
}

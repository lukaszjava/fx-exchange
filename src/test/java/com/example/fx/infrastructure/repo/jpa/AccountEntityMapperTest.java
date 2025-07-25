package com.example.fx.infrastructure.repo.jpa;

import static org.assertj.core.api.Assertions.*;

import com.example.fx.domain.model.Account;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AccountEntityMapperTest {

  AccountEntityMapper mapper = Mappers.getMapper(AccountEntityMapper.class);

  @Test
  void toEntity_and_back() {
    Account domain =
        new Account(
            UUID.randomUUID(), "J", "K", new BigDecimal("10.00"), new BigDecimal("2.0000"), 3);

    AccountEntity entity = mapper.toEntity(domain);
    assertThat(entity.getVersion()).isEqualTo(3);

    Account back = mapper.toDomain(entity);
    assertThat(back.id()).isEqualTo(domain.id());
    assertThat(back.version()).isEqualTo(3);
  }
}

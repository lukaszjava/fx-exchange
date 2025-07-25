package com.example.fx.infrastructure.repo.jpa;

import com.example.fx.domain.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountEntityMapper {

  Account toDomain(AccountEntity entity);

  AccountEntity toEntity(Account account);
}

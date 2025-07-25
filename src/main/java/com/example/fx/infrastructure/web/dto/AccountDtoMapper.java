package com.example.fx.infrastructure.web.dto;

import com.example.fx.application.service.ExchangeFundsHandler;
import com.example.fx.domain.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountDtoMapper {

  @Mapping(target = "accountId", source = "id")
  @Mapping(target = "balances.PLN", source = "pln")
  @Mapping(target = "balances.USD", source = "usd")
  AccountResponse toResponse(Account acc);

  @Mapping(target = "balances.PLN", source = "account.pln")
  @Mapping(target = "balances.USD", source = "account.usd")
  @Mapping(target = "rate", source = "rate")
  @Mapping(target = "rateTimestamp", source = "timestamp")
  ExchangeResponse toResponse(ExchangeFundsHandler.Result result);
}

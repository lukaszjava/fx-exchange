package com.example.fx.infrastructure.web;

import com.example.fx.application.port.in.ExchangeFundsUseCase;
import com.example.fx.domain.model.Direction;
import com.example.fx.infrastructure.web.dto.AccountDtoMapper;
import com.example.fx.infrastructure.web.dto.ExchangeRequest;
import com.example.fx.infrastructure.web.dto.ExchangeResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts/{id}/exchange")
@RequiredArgsConstructor
public class ExchangeController {

  private final ExchangeFundsUseCase exchangeFundsUseCase;
  private final AccountDtoMapper mapper;

  @PostMapping
  public ExchangeResponse exchange(
      @PathVariable UUID id, @RequestBody @Validated ExchangeRequest request) {
    Direction direction =
        request.direction() == ExchangeRequest.Direction.PLN_TO_USD
            ? Direction.PLN_TO_USD
            : Direction.USD_TO_PLN;

    var result = exchangeFundsUseCase.exchange(id, direction, request.amount());
    return mapper.toResponse(result);
  }
}

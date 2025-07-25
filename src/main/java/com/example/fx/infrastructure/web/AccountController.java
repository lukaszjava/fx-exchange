package com.example.fx.infrastructure.web;

import com.example.fx.application.port.in.CreateAccountUseCase;
import com.example.fx.application.port.in.GetAccountUseCase;
import com.example.fx.infrastructure.web.dto.AccountDtoMapper;
import com.example.fx.infrastructure.web.dto.AccountResponse;
import com.example.fx.infrastructure.web.dto.CreateAccountRequest;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final CreateAccountUseCase createAccountUseCase;
  private final GetAccountUseCase getAccountUseCase;
  private final AccountDtoMapper mapper;

  @PostMapping
  public ResponseEntity<AccountResponse> create(@RequestBody @Validated CreateAccountRequest req) {
    UUID id = createAccountUseCase.create(req.firstName(), req.lastName(), req.initialPlnBalance());
    return ResponseEntity.created(URI.create("/accounts/" + id))
        .body(mapper.toResponse(getAccountUseCase.get(id)));
  }

  @GetMapping("/{id}")
  public AccountResponse get(@PathVariable UUID id) {
    return mapper.toResponse(getAccountUseCase.get(id));
  }
}

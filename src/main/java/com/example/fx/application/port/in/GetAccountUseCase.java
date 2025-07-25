package com.example.fx.application.port.in;

import com.example.fx.domain.model.Account;
import java.util.UUID;

public interface GetAccountUseCase {
  Account get(UUID id);
}

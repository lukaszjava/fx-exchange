package com.example.fx.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateAccountUseCase {
  UUID create(String firstName, String lastName, BigDecimal initialPln);
}

package com.example.fx.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateAccountRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal initialPlnBalance) {}

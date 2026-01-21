package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequestDto(
        @NotNull(message = "Source card ID is required")
        Long sourceCardId,

        @NotNull(message = "Target card ID is required")
        Long targetCardId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {}
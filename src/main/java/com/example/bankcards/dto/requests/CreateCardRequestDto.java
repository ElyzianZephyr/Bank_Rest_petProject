package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateCardRequestDto(
        @NotNull(message = "User ID is required")
        Long userId,

        @PositiveOrZero(message = "Initial balance cannot be negative")
        BigDecimal initialBalance
) {}
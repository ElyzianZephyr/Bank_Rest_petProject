package com.example.bankcards.dto.requests;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCardStatusRequestDto(
        @NotNull(message = "Status is required")
        CardStatus status
) {}
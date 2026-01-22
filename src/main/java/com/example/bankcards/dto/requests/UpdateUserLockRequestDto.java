package com.example.bankcards.dto.requests;

import jakarta.validation.constraints.NotNull;

public record UpdateUserLockRequestDto(
        @NotNull(message = "Lock status is required")
        Boolean isLocked
) {}
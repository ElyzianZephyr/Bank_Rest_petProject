package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.Role;

public record UserResponseDto(
        Long id,
        String username,
        Role role,
        boolean isLocked
) {}
package com.example.bankcards.dto.response;

import com.example.bankcards.entity.Client;
import com.example.bankcards.entity.enums.Role;

public record UserResponseDto(
        Long id,
        String username,
        Role role,
        boolean isLocked
) {
    public static  UserResponseDto from(Client client) {
        return new UserResponseDto(
                client.getId(),
                client.getUsername(),
                client.getRole(),
                client.isLocked()
        );
    }
}
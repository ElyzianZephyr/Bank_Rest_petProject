package com.example.bankcards.service.interfaces;


import com.example.bankcards.dto.requests.AuthRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(AuthRequestDto request);
    AuthResponseDto register(AuthRequestDto request);
}
package com.example.bankcards.service;

import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.dto.requests.TransferRequestDto;
import java.util.List;

public interface CardService {
    /**
     * Retrieves all cards belonging to the current user.
     * @return List of masked card details.
     */
    List<CardResponseDto> getMyCards();

    /**
     * Executes a fund transfer between two cards.
     * @param request Transfer details (source, target, amount).
     */
    void transfer(TransferRequestDto request);
}
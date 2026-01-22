package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.requests.CreateCardRequestDto;
import com.example.bankcards.dto.requests.TransferRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.dto.response.PageResponseDto;
import com.example.bankcards.entity.enums.CardStatus;

import java.util.List;

public interface CardService {
    /**
     * Retrieves all cards belonging to the current user with pagination and optional search.
     * @param page Page number (0-based).
     * @param size Page size.
     * @param query Optional search query (partial card number).
     * @return Paginated list of masked card details.
     */
    PageResponseDto<CardResponseDto> getMyCards(int page, int size, String query);

    /**
     * Blocks a card owned by the current user.
     * @param cardId The ID of the card to block.
     */
    void blockMyCard(Long cardId);
    /**
     * Executes a fund transfer between two cards.
     * @param request Transfer details (source, target, amount).
     */
    void transfer(TransferRequestDto request);

    /**
     * Creates a new card for a specific user.
     * @param request Details including user ID and initial balance.
     * @return The created card details.
     */
    CardResponseDto createCard(CreateCardRequestDto request);

    /**
     * Updates the status of a specific card (e.g., BLOCK or ACTIVATE).
     * @param cardId The ID of the card.
     * @param status The new status to apply.
     * @return The updated card details.
     */
    CardResponseDto updateCardStatus(Long cardId, CardStatus status);

    /**
     * Retrieves all cards existing in the system.
     * @return List of all cards.
     */
    List<CardResponseDto> getAllCards();

    /**
     * Retrieves a specific card by its unique identifier.
     * @param cardId The card ID.
     * @return The card details.
     */
    CardResponseDto getCardById(Long cardId);

    /**
     * Deletes a card from the database.
     * @param cardId The ID of the card to delete.
     */
    void deleteCard(Long cardId);
}
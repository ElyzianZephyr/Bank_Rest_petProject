package com.example.bankcards.service.impl;

import com.example.bankcards.dto.requests.TransferRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Client;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AppSecurityException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.RestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CardResponseDto> getMyCards() {
        Client principal = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return cardRepository.findAllByOwnerId(principal.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional // Ensures atomicity: both updates succeed, or neither does.
    public void transfer(TransferRequestDto request) {
        // 0. Prevent transfer to the same card
        if (request.sourceCardId().equals(request.targetCardId())) {
            throw new RestException("Cannot transfer to the same card", HttpStatus.BAD_REQUEST);
        }

        // 1. Get authenticated user
        Client principal = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Fetch Cards
        // Note: We fetch entities to manage them in the persistence context.
        Card sourceCard = cardRepository.findById(request.sourceCardId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));

        Card targetCard = cardRepository.findById(request.targetCardId())
                .orElseThrow(() -> new CardNotFoundException("Target card not found"));

        // 3. Validate Ownership
        // Security Rule: User can only spend money from THEIR OWN card.
        // (Assuming target card can be anyone's, but if requirements say "between my cards", add check for target too)
        if (!Objects.equals(sourceCard.getOwner().getId(), principal.getId())) {
            throw new AppSecurityException("You do not own the source card");
        }

        // Per controller description "Transfer money between two cards owned by the user", we validate target too:
        if (!Objects.equals(targetCard.getOwner().getId(), principal.getId())) {
            throw new AppSecurityException("Target card does not belong to you");
        }

        // 4. Validate Status
        if (sourceCard.getStatus() != CardStatus.ACTIVE) {
            throw new RestException("Source card is not ACTIVE", HttpStatus.BAD_REQUEST);
        }
        if (targetCard.getStatus() != CardStatus.ACTIVE) {
            throw new RestException("Target card is not ACTIVE", HttpStatus.BAD_REQUEST);
        }

        // 5. Validate Balance
        if (sourceCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card");
        }

        // 6. Execute Transfer
        // Optimistic locking (@Version in Entity) will automatically check for concurrent modifications here.
        sourceCard.setBalance(sourceCard.getBalance().subtract(request.amount()));
        targetCard.setBalance(targetCard.getBalance().add(request.amount()));

        // 7. Save
        cardRepository.save(sourceCard);
        cardRepository.save(targetCard);
    }

    // ... Helper methods (mapToDto, maskCardNumber) from previous step ...
    private CardResponseDto mapToDto(Card card) {
        return new CardResponseDto(
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                card.getBalance(),
                card.getStatus().name(),
                card.getValidityDate()
        );
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
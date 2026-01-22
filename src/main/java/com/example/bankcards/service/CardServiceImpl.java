package com.example.bankcards.service;

import com.example.bankcards.dto.requests.CreateCardRequestDto;
import com.example.bankcards.dto.requests.TransferRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Client;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.RestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.ClientRepository;
import com.example.bankcards.service.interfaces.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ClientRepository clientRepository;
    private final Random random = new Random();

    @Override
    @Transactional(readOnly = true)
    public List<CardResponseDto> getMyCards() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByUsername(username)
                .orElseThrow(() -> new RestException("User not found", HttpStatus.NOT_FOUND));

        return cardRepository.findAllByOwnerId(client.getId()).stream()
                .map(this::toCardResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void transfer(TransferRequestDto request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Fetch Source Card and validate ownership
        Card sourceCard = cardRepository.findById(request.sourceCardId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));

        if (!sourceCard.getOwner().getUsername().equals(username)) {
            throw new RestException("You can only transfer funds from your own cards", HttpStatus.FORBIDDEN);
        }

        if (sourceCard.getStatus().equals(CardStatus.BLOCKED.name()) ||
                sourceCard.getStatus().equals(CardStatus.EXPIRED.name())) {
            throw new RestException("Source card is not active", HttpStatus.BAD_REQUEST);
        }

        // 2. Fetch Target Card
        Card targetCard = cardRepository.findById(request.targetCardId())
                .orElseThrow(() -> new CardNotFoundException("Target card not found"));

        if (targetCard.getStatus().equals(CardStatus.BLOCKED.name()) ||
                targetCard.getStatus().equals(CardStatus.EXPIRED.name())) {
            throw new RestException("Target card is not active", HttpStatus.BAD_REQUEST);
        }

        // 3. Validate Balance
        if (sourceCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // 4. Execute Transfer
        sourceCard.setBalance(sourceCard.getBalance().subtract(request.amount()));
        targetCard.setBalance(targetCard.getBalance().add(request.amount()));

        cardRepository.save(sourceCard);
        cardRepository.save(targetCard);
    }

    @Override
    @Transactional
    public CardResponseDto createCard(CreateCardRequestDto request) {
        Client owner = clientRepository.findById(request.userId())
                .orElseThrow(() -> new RestException("User with ID " + request.userId() + " not found", HttpStatus.NOT_FOUND));

        Card card = new Card();
        card.setCardNumber(generateRandomCardNumber());
        card.setOwner(owner);
        card.setBalance(request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);
        card.setValidityDate(LocalDate.now().plusYears(3));

        Card savedCard = cardRepository.save(card);
        return toCardResponseDto(savedCard);
    }

    @Override
    @Transactional
    public CardResponseDto updateCardStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with ID " + cardId + " not found"));

        card.setStatus(status);
        Card updatedCard = cardRepository.save(card);
        return toCardResponseDto(updatedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponseDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::toCardResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponseDto getCardById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        return toCardResponseDto(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Card with ID " + cardId + " not found");
        }
        cardRepository.deleteById(cardId);
    }

    // --- Helper Methods ---

    private CardResponseDto toCardResponseDto(Card card) {
        return new CardResponseDto(
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                card.getBalance(),
                card.getStatus(),
                card.getValidityDate()
        );
    }


    // TODO
    private String generateRandomCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String maskCardNumber(String clearCardNumber) {
        if (clearCardNumber == null || clearCardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + clearCardNumber.substring(clearCardNumber.length() - 4);
    }
}
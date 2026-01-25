package com.example.bankcards.dto.response;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponseDto(
   Long id,
   String maskCardNumber,
   BigDecimal balance,
   CardStatus status,
   LocalDate validityDate
) {
    private String maskCardNumber(String clearCardNumber) {
        if (clearCardNumber == null || clearCardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + clearCardNumber.substring(clearCardNumber.length() - 4);
    }
    public static CardResponseDto from(Card card) {
        return new CardResponseDto(
                card.getId(),
                card.getMaskCardNumber(),
                card.getBalance(),
                card.getStatus(),
                card.getValidityDate()
        );
    }
}

package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponseDto(
   Long id,
   String maskCardNumber,
   BigDecimal balance,
   CardStatus status,
   LocalDate validityDate
) {}

package com.example.bankcards.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponseDto(
   Long id,
   String maskCardNumber,
   BigDecimal balance,
   String status,
   LocalDate validityDate
) {}

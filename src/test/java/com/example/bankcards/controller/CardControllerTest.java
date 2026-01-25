package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.TransferRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.dto.response.PageResponseDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.security.ClientDetailsServiceImpl;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.interfaces.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- БИЗНЕС-ЛОГИКА (Зависимость контроллера) ---
    @MockitoBean
    private CardService cardService;

    // --- ИНФРАСТРУКТУРА SECURITY (Нужны чтобы поднять Context) ---
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ClientDetailsServiceImpl clientDetailsService;

    @Test
    @DisplayName("GET /api/v1/cards - Success")
    void getMyCards_ShouldReturnPageOfCards() throws Exception {
        // Arrange
        CardResponseDto cardDto = new CardResponseDto(
                1L,
                "1234****5678",
                BigDecimal.TEN,
                CardStatus.ACTIVE,
                LocalDate.now().plusYears(3)
        );

        // Предполагаем, что конструктор PageResponseDto соответствует этому вызову
        PageResponseDto<CardResponseDto> pageResponse = new PageResponseDto<>(
                List.of(cardDto), 0, 10, 1, 1
        );

        when(cardService.getMyCards(anyInt(), anyInt(), any())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/cards/transfer - Success")
    void transfer_ShouldReturn200_WhenTransferIsSuccessful() throws Exception {
        // Arrange
        TransferRequestDto request = new TransferRequestDto(1L, 2L, BigDecimal.valueOf(100));

        doNothing().when(cardService).transfer(any(TransferRequestDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/cards/transfer - Bad Request (Negative Amount)")
    void transfer_ShouldReturn400_WhenAmountIsNegative() throws Exception {
        // Arrange
        // Валидация (@Min / @Positive) должна сработать в DTO
        TransferRequestDto request = new TransferRequestDto(1L, 2L, BigDecimal.valueOf(-100));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/cards/{id}/block - Success")
    void blockCard_ShouldReturn200_WhenCardExists() throws Exception {
        // Arrange
        Long cardId = 1L;
        doNothing().when(cardService).blockMyCard(cardId);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/cards/{id}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
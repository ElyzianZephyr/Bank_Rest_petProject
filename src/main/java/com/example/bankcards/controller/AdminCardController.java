package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.CreateCardRequestDto;
import com.example.bankcards.dto.requests.UpdateCardStatusRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.service.interfaces.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@Tag(name = "Admin Cards", description = "Administrative Card Management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Create Card", description = "Issue a new card for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CreateCardRequestDto request) {
        return new ResponseEntity<>(cardService.createCard(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get All Cards", description = "Retrieve a list of all cards in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of cards retrieved successfully")
    })
    public ResponseEntity<List<CardResponseDto>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Get Card by ID", description = "Retrieve details of a specific card by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    @PatchMapping("/{cardId}/status")
    @Operation(summary = "Update Card Status", description = "Change the status of a card (e.g., BLOCK, ACTIVATE).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardResponseDto> updateCardStatus(
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardStatusRequestDto request) {
        return ResponseEntity.ok(cardService.updateCardStatus(cardId, request.status()));
    }
}
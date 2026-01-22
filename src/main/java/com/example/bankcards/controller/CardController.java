package com.example.bankcards.controller;


import com.example.bankcards.dto.requests.TransferRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.dto.response.PageResponseDto;
import com.example.bankcards.service.interfaces.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Card Management and Money Transfers")
@SecurityRequirement(name = "bearerAuth") // Requires JWT for Swagger calls
public class CardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Get My Cards", description = "Retrieve a paged list of active cards belonging to the authenticated user. Supports optional search by partial card number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of cards retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<PageResponseDto<CardResponseDto>> getMyCards(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Partial card number to search for")
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(cardService.getMyCards(page, size, query));
    }

    @PatchMapping("/{cardId}/block")
    @Operation(summary = "Block Card", description = "Block a specific card owned by the user. This action cannot be undone via this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card blocked successfully"),
            @ApiResponse(responseCode = "403", description = "You do not have permission to block this card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> blockCard(
            @Parameter(description = "ID of the card to block")
            @PathVariable Long cardId
    ) {
        cardService.blockMyCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer Funds", description = "Transfer money between two cards owned by the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequestDto request) {
        cardService.transfer(request);
        return ResponseEntity.ok().build();
    }
}
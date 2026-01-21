package com.example.bankcards.controller;


import com.example.bankcards.dto.requests.TransferRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Get My Cards", description = "Retrieve a list of all active cards belonging to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of cards retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<List<CardResponseDto>> getMyCards() {
        return ResponseEntity.ok(cardService.getMyCards());
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
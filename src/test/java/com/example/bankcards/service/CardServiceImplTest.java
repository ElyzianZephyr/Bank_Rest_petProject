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
import com.example.bankcards.service.interfaces.CardNumberGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @Mock private CardNumberGenerator cardNumberGenerator;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================================================================================
    // 3.1 Card Creation
    // ==================================================================================
    @Nested
    @DisplayName("Card Creation")
    class CreateCardTests {

        @Test
        @DisplayName("Success: Creates card with Default Balance 0 and Status ACTIVE")
        void createCard_Success() {
            // Arrange
            Long userId = 1L;
            CreateCardRequestDto request = new CreateCardRequestDto(userId, null); // Null balance -> 0

            Client client = new Client();
            client.setId(userId);

            when(clientRepository.findById(userId)).thenReturn(Optional.of(client));
            // Return the card passed to save()
            when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));
            when(cardNumberGenerator.generate()).thenReturn("1111222233334444");

            // Act
            CardResponseDto response = cardService.createCard(request);

            // Assert
            ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
            verify(cardRepository).save(cardCaptor.capture());
            Card savedCard = cardCaptor.getValue();

            assertThat(savedCard.getOwner()).isEqualTo(client);
            assertThat(savedCard.getStatus()).isEqualTo(CardStatus.ACTIVE);
            assertThat(savedCard.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(savedCard.getValidityDate()).isAfter(LocalDate.now());
            assertThat(savedCard.getCardNumber()).hasSize(16); // Check random generation length

            // Validate Response mapping
            assertThat(response.status()).isEqualTo(CardStatus.ACTIVE);
            assertThat(response.maskCardNumber()).contains("****"); // Ensure response is masked
        }

        @Test
        @DisplayName("Fail: Throws Exception if User not found")
        void createCard_Fail_UserNotFound() {
            CreateCardRequestDto request = new CreateCardRequestDto(999L, BigDecimal.TEN);
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cardService.createCard(request))
                    .isInstanceOf(RestException.class)
                    .hasMessageContaining("User with ID 999 not found");

            verify(cardRepository, never()).save(any());
        }
    }

    // ==================================================================================
    // 3.2 Card Viewing (Masking)
    // ==================================================================================
    @Nested
    @DisplayName("Card Viewing & Masking")
    class ViewCardTests {

        @Test
        @DisplayName("Get My Cards: Returns Masked Numbers (**** 1234)")
        void getMyCards_Masking_Success() {
            // Arrange
            String username = "testuser";
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);

            Client client = new Client();
            client.setId(10L);
            when(clientRepository.findByUsername(username)).thenReturn(Optional.of(client));

            Card card = new Card();
            card.setId(1L);
            card.setCardNumber("1111222233334444"); // RAW DB Data
            card.setOwner(client);
            card.setBalance(new BigDecimal("100.00"));
            card.setStatus(CardStatus.ACTIVE);
            card.setValidityDate(LocalDate.now().plusYears(1));


            when(cardRepository.findAllByOwnerId(eq(10L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(card)));

            // Act
            // NOTE: If this line fails compilation with "Expected 3 args", your local interface
            // has pagination parameters (e.g., page, size, sort). Update the test call accordingly.
            List<CardResponseDto> results = cardService.getMyCards(0,20, null).content();

            // Assert
            assertThat(results).hasSize(1);
            CardResponseDto dto = results.get(0);

            // SECURITY CHECK: Ensure full number is NOT leaked
            assertThat(dto.maskCardNumber()).doesNotContain("11112222");
            assertThat(dto.maskCardNumber()).contains("****");
            assertThat(dto.maskCardNumber()).endsWith("4444");
        }
    }

    // ==================================================================================
    // 3.3 Internal Transfers
    // ==================================================================================
    @Nested
    @DisplayName("Transfer Logic")
    class TransferTests {

        @Test
        @DisplayName("Happy Path: Transfers money and updates BOTH balances")
        void transfer_Success() {
            // Arrange
            String currentUser = "user1";
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(currentUser);

            // Source Card (Owner = currentUser)
            Card source = createMockCard(100L, currentUser, "100.00", CardStatus.ACTIVE);
            // Target Card
            Card target = createMockCard(200L, "otherUser", "50.00", CardStatus.ACTIVE);

            when(cardRepository.findById(100L)).thenReturn(Optional.of(source));
            when(cardRepository.findById(200L)).thenReturn(Optional.of(target));

            TransferRequestDto request = new TransferRequestDto(100L, 200L, new BigDecimal("30.00"));

            // Act
            cardService.transfer(request);

            // Assert Atomic Transaction
            assertThat(source.getBalance()).isEqualByComparingTo("70.00");
            assertThat(target.getBalance()).isEqualByComparingTo("80.00");

            verify(cardRepository).save(source);
            verify(cardRepository).save(target);
        }

        @Test
        @DisplayName("Insufficient Funds: Throws Exception, Balances Unchanged")
        void transfer_Fail_InsufficientFunds() {
            // Arrange
            String currentUser = "user1";
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(currentUser);

            Card source = createMockCard(100L, currentUser, "10.00", CardStatus.ACTIVE);
            Card target = createMockCard(200L, "otherUser", "50.00", CardStatus.ACTIVE);

            when(cardRepository.findById(100L)).thenReturn(Optional.of(source));
            when(cardRepository.findById(200L)).thenReturn(Optional.of(target));

            TransferRequestDto request = new TransferRequestDto(100L, 200L, new BigDecimal("20.00"));

            // Act & Assert
            assertThatThrownBy(() -> cardService.transfer(request))
                    .isInstanceOf(InsufficientFundsException.class);

            // Verify Balances Unchanged
            assertThat(source.getBalance()).isEqualByComparingTo("10.00");
            assertThat(target.getBalance()).isEqualByComparingTo("50.00");
            verify(cardRepository, never()).save(any());
        }

        @Test
        @DisplayName("Security (IDOR): Cannot transfer from card owned by another user")
        void transfer_Fail_NotOwner() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("hacker"); // Current User

            Card victimCard = createMockCard(100L, "victim", "1000.00", CardStatus.ACTIVE);

            when(cardRepository.findById(100L)).thenReturn(Optional.of(victimCard));

            TransferRequestDto request = new TransferRequestDto(100L, 200L, BigDecimal.TEN);

            // Act & Assert
            assertThatThrownBy(() -> cardService.transfer(request))
                    .isInstanceOf(RestException.class)
                    .hasMessageContaining("transfer funds from your own cards");

            verify(cardRepository, never()).save(any());
        }

        @Test
        @DisplayName("Validation: Cannot transfer from BLOCKED card")
        void transfer_Fail_SourceBlocked() {
            // Arrange
            String currentUser = "user1";
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(currentUser);

            Card source = createMockCard(100L, currentUser, "100.00", CardStatus.BLOCKED);

            when(cardRepository.findById(100L)).thenReturn(Optional.of(source));

            TransferRequestDto request = new TransferRequestDto(100L, 200L, BigDecimal.TEN);

            // Act & Assert
            // WARNING: This test will FAIL if 'sourceCard.getStatus().equals(CardStatus.BLOCKED.name())'
            // is not fixed. Enum != String.
            assertThatThrownBy(() -> cardService.transfer(request))
                    .isInstanceOf(RestException.class)
                    .hasMessageContaining("Source card is not active");

            verify(cardRepository, never()).save(any());
        }
    }

    // ==================================================================================
    // 3.4 Admin Operations
    // ==================================================================================
    @Nested
    @DisplayName("Admin Operations")
    class AdminTests {

        @Test
        @DisplayName("Update Status: Successfully updates card status")
        void updateCardStatus_Success() {
            // Arrange
            Card card = new Card();
            card.setId(55L);
            card.setStatus(CardStatus.ACTIVE);

            when(cardRepository.findById(55L)).thenReturn(Optional.of(card));
            when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            CardResponseDto response = cardService.updateCardStatus(55L, CardStatus.EXPIRED);

            // Assert
            assertThat(card.getStatus()).isEqualTo(CardStatus.EXPIRED);
            assertThat(response.status()).isEqualTo(CardStatus.EXPIRED);
            verify(cardRepository).save(card);
        }
    }

    // Helper
    private Card createMockCard(Long id, String ownerUsername, String balance, CardStatus status) {
        Card card = new Card();
        card.setId(id);
        card.setBalance(new BigDecimal(balance));
        card.setStatus(status);
        Client owner = new Client();
        owner.setUsername(ownerUsername);
        card.setOwner(owner);
        return card;
    }
}
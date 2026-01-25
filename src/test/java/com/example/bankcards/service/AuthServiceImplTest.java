package com.example.bankcards.service;

import com.example.bankcards.dto.requests.AuthRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.entity.Client;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.RestException;
import com.example.bankcards.repository.ClientRepository;
import com.example.bankcards.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should register user successfully when username is unique")
    void shouldRegisterUser_WhenUsernameIsUnique() {
        // Arrange
        String username = "newUser";
        String password = "password";
        String encodedPassword = "hashedPass";
        String generatedToken = "jwt.token.value";

        AuthRequestDto request = new AuthRequestDto(username, password);

        // Моки для проверки уникальности и хеширования
        given(clientRepository.existsByUsername(username)).willReturn(false);
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);

        // Моки для последующего логина (внутри метода register)
        Authentication authentication = mock(Authentication.class);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtUtils.generateToken(authentication)).willReturn(generatedToken);

        // Act
        AuthResponseDto response = authService.register(request);

        // Assert
        // 1. Проверяем сохранение клиента с правильными данными
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());

        Client savedClient = clientCaptor.getValue();
        assertThat(savedClient.getUsername()).isEqualTo(username);
        assertThat(savedClient.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedClient.getRole()).isEqualTo(Role.ROLE_USER); //

        // 2. Проверяем, что вернулся токен (результат логина)
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(generatedToken);
    }

    @Test
    @DisplayName("Should throw RestException when username is already taken")
    void shouldThrowException_WhenUserAlreadyExists() {
        // Arrange
        String username = "existingUser";
        AuthRequestDto request = new AuthRequestDto(username, "password");

        given(clientRepository.existsByUsername(username)).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RestException.class)
                .hasMessage("Username is already taken"); //
    }
}
package com.example.bankcards.controller;

import com.example.bankcards.dto.requests.AuthRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.exception.RestException;
import com.example.bankcards.security.ClientDetailsServiceImpl;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- MOCKS FOR CONTROLLER DEPENDENCIES ---

    @MockitoBean
    private AuthService authService;



    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ClientDetailsServiceImpl clientDetailsService;

    // --- TESTS ---

    @Test
    @DisplayName("POST /api/auth/signup - Success")
    void register_ShouldReturnToken_WhenRequestIsValid() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("user", "password123");
        AuthResponseDto response = new AuthResponseDto("jwt-token-example");

        when(authService.register(any(AuthRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-example"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success")
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("user", "password123");
        AuthResponseDto response = new AuthResponseDto("jwt-token-example");

        when(authService.login(any(AuthRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-example"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - Bad Request (Validation)")
    void register_ShouldReturn400_WhenUsernameIsEmpty() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("", "pass");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signup - Conflict (User already exists)")
    void register_ShouldReturn409_WhenUserAlreadyExists() throws Exception {
        // Arrange
        AuthRequestDto request = new AuthRequestDto("existingUser", "password");

        when(authService.register(any(AuthRequestDto.class)))
                .thenThrow(new RestException("Username is already taken", HttpStatus.CONFLICT));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }
}
package com.example.bankcards.exception;

import com.example.bankcards.dto.response.ErrorResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;



import static org.assertj.core.api.Assertions.assertThat;


class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Business Exception: InsufficientFundsException returns 400")
    void handleInsufficientFundsException() {
        // Arrange
        InsufficientFundsException ex = new InsufficientFundsException("Not enough money");

        // Act
        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleRestException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Not enough money");
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Security Exception: AppSecurityException returns 403")
    void handleAppSecurityException() {
        // Arrange
        AppSecurityException ex = new AppSecurityException("Access denied");

        // Act
        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleRestException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Access denied");
        assertThat(response.getBody().status()).isEqualTo(403);
    }

    @Test
    @DisplayName("System Exception: Generic Exception returns 500 and hides stack trace")
    void handleGlobalException() {
        // Arrange
        RuntimeException ex = new RuntimeException("Database connection failed");

        // Act
        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleGlobalException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        // Проверяем, что сообщение обернуто в "An unexpected error occurred", а не просто вываливает стек
        assertThat(response.getBody().message()).startsWith("An unexpected error occurred: Database connection failed");
    }

    @Test
    @DisplayName("Validation Exception: Returns 400 with field errors")
    void handleValidationExceptions() {
        // Arrange
        // Вместо мока используем реальную реализацию BindingResult
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");

        // Добавляем ошибки напрямую в BindingResult
        bindingResult.addError(new FieldError("object", "amount", "must be positive"));
        bindingResult.addError(new FieldError("object", "cardNumber", "must not be blank"));

        // MethodParameter может быть null, так как он не используется в вашем обработчике
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        // Проверяем наличие сообщений об ошибках
        String message = response.getBody().message();
        assertThat(message).contains("amount: must be positive");
        assertThat(message).contains("cardNumber: must not be blank");
    }
}
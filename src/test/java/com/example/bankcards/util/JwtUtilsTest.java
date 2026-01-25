package com.example.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // ИСХОДНЫЙ КЛЮЧ (32 символа = 256 бит) закодированный в Base64
    // "12345678901234567890123456789012" в Base64 ->
    private static final String SECRET_KEY_BASE64 = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    private static final long EXPIRATION_MS = 60000; // 1 minute

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(SECRET_KEY_BASE64, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Token Generation: Should generate a non-empty JWT string")
    void generateToken_Success() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        // Проверяем, что это валидный JWT (состоит из 3 частей, разделенных точкой)
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Get Username: Should extract correct username from token")
    void getUsernameFromToken_Success() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        String extractedUsername = jwtUtils.getUsernameFromToken(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Validation: Valid token returns true")
    void validateToken_Valid_ReturnsTrue() {
        String token = jwtUtils.generateToken("testuser");
        boolean isValid = jwtUtils.validateToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Validation: Malformed token returns false")
    void validateToken_Malformed_ReturnsFalse() {
        String malformedToken = "invalid.token.structure";
        boolean isValid = jwtUtils.validateToken(malformedToken);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Validation: Expired token returns false")
    void validateToken_Expired_ReturnsFalse() throws InterruptedException {
        // Создаем экземпляр с очень коротким временем жизни (10 мс)
        // Используем тот же корректный Base64 ключ
        JwtUtils shortLivedJwtUtils = new JwtUtils(SECRET_KEY_BASE64, 10);
        String token = shortLivedJwtUtils.generateToken("quick_user");

        // Ждем пока токен протухнет
        Thread.sleep(50);

        boolean isValid = shortLivedJwtUtils.validateToken(token);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Validation: Null or Empty token returns false (Robustness)")
    void validateToken_NullOrEmpty_ReturnsFalse() {
        assertThat(jwtUtils.validateToken(null)).isFalse();
        assertThat(jwtUtils.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("Security: Token signed with a different key is invalid")
    void validateToken_WrongKey_ReturnsFalse() {
        // 1. Генерируем токен основным ключом
        String token = jwtUtils.generateToken("hacker");

        // 2. Создаем другой экземпляр с ДРУГИМ ключом
        // Другой ключ "98765432109876543210987654321098" закодированный в Base64 ->
        String differentSecretBase64 = "OTg3NjU0MzIxMDk4NzY1NDMyMTA5ODc2NTQzMjEwOTg=";
        JwtUtils otherKeyUtils = new JwtUtils(differentSecretBase64, EXPIRATION_MS);

        // 3. Пытаемся валидировать токен, подписанный ключом А, с помощью ключа Б
        // (В оригинальном коде была ошибка: вы вызывали jwtUtils.validateToken(token), а надо otherKeyUtils)
        // Но даже если вы хотели проверить, примет ли 'jwtUtils' чужой токен (сгенерированный otherKeyUtils):

        String alienToken = otherKeyUtils.generateToken("hacker");

        // jwtUtils не должен принять токен, подписанный другим ключом
        boolean isValid = jwtUtils.validateToken(alienToken);

        assertThat(isValid).isFalse();
    }
}
package com.example.bankcards.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // Must be at least 256 bits (32 bytes) for HS256
    private static final String SECRET_KEY = "12345678901234567890123456789012";
    private static final long EXPIRATION_MS = 60000; // 1 minute

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(SECRET_KEY, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Token Generation: Should generate a non-empty JWT string")
    void generateToken_Success() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        // JWT structure: Header.Payload.Signature (2 dots)
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Extraction: Should correctly extract username from a valid token")
    void getUsernameFromToken_Success() {
        String username = "security_admin";
        String token = jwtUtils.generateToken(username);

        String extractedUsername = jwtUtils.getUsernameFromToken(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Validation: Valid token returns true")
    void validateToken_Valid_ReturnsTrue() {
        String token = jwtUtils.generateToken("valid_user");

        boolean isValid = jwtUtils.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Validation: Malformed/Tampered token returns false")
    void validateToken_Malformed_ReturnsFalse() {
        String token = jwtUtils.generateToken("user");
        String tamperedToken = token + "sabotage";

        boolean isValid = jwtUtils.validateToken(tamperedToken);
        boolean isValidGarbage = jwtUtils.validateToken("invalid.token.garbage");

        assertThat(isValid).isFalse();
        assertThat(isValidGarbage).isFalse();
    }

    @Test
    @DisplayName("Validation: Expired token returns false")
    void validateToken_Expired_ReturnsFalse() throws InterruptedException {
        // Create a separate instance with a very short expiration (10ms)
        JwtUtils shortLivedJwtUtils = new JwtUtils(SECRET_KEY, 10);
        String token = shortLivedJwtUtils.generateToken("quick_user");

        // Wait for token to expire
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
        // Generate a token using the main utils
        String token = jwtUtils.generateToken("hacker");

        // Create a DIFFERENT utility with a DIFFERENT key
        String differentSecret = "98765432109876543210987654321098";
        JwtUtils otherKeyUtils = new JwtUtils(differentSecret, EXPIRATION_MS);

        // Try to validate the original token with the wrong key
        boolean isValid = otherKeyUtils.validateToken(token);

        assertThat(isValid).isFalse();
    }
}
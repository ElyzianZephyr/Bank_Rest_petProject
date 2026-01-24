package com.example.bankcards.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberAttributeConverterTest {

    private CardNumberAttributeConverter converter;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Generate a real valid 128-bit AES key for the test context
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        converter = new CardNumberAttributeConverter(base64Key);
    }

    @Test
    @DisplayName("Symmetry: Encrypting and then Decrypting returns original value")
    void testEncryptionDecryptionSymmetry() {
        String originalCardNumber = "1234567812345678";

        String encrypted = converter.convertToDatabaseColumn(originalCardNumber);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(encrypted).isNotEqualTo(originalCardNumber); // Ensure it actually changed
        assertThat(decrypted).isEqualTo(originalCardNumber);    // Ensure it came back
    }

    @Test
    @DisplayName("Security: Same input produces different ciphertext (IV Randomness)")
    void testIvRandomness() {
        String originalCardNumber = "1111222233334444";

        String run1 = converter.convertToDatabaseColumn(originalCardNumber);
        String run2 = converter.convertToDatabaseColumn(originalCardNumber);

        // If these are equal, the encryption is deterministic (bad for security)
        // or the IV is static.
        assertThat(run1).isNotEqualTo(run2);

        // However, both should decrypt to the same value
        assertThat(converter.convertToEntityAttribute(run1)).isEqualTo(originalCardNumber);
        assertThat(converter.convertToEntityAttribute(run2)).isEqualTo(originalCardNumber);
    }

    @Test
    @DisplayName("Robustness: Null inputs return null without exceptions")
    void testNullHandling() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
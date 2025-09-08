package com.example.bankcards.util;

import com.example.bankcards.exception.DecryptionException;
import com.example.bankcards.exception.EncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CardEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private final SecretKey secretKey;

    public CardEncryptionService(@Value("${app.card.encryption-key}") String encryptionKey) {
        this.secretKey = generateKeyFromString(encryptionKey);
    }
    public String encryptCardNumber(String cardNumber) {
        System.out.println(cardNumber);
        validateCardNumber(cardNumber);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] iv = generateRandomIV();
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            byte[] encryptedData = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);

            String result = Base64.getEncoder().encodeToString(encryptedWithIv);
            return result;

        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt card number", e);
        }
    }

    public String decryptCardNumber(String encryptedCardNumber) {
        if (encryptedCardNumber == null || encryptedCardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Encrypted card number cannot be null or empty");
        }

        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedCardNumber);

            if (encryptedWithIv.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }

            // Извлекаем IV и зашифрованные данные
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];

            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            String result = new String(decryptedData, StandardCharsets.UTF_8);

            return result;

        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt card number", e);
        }
    }

    private byte[] generateRandomIV() throws NoSuchAlgorithmException {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        return iv;
    }
    private SecretKey generateKeyFromString(String encryptionKey) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be null or empty");
        }

        String cleanNumber = cardNumber.replaceAll("[\\s+-]", "");
        System.out.println(cleanNumber);
        if (!cleanNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException("Card number must contain exactly 16 digits");
        }

    }

}

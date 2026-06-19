package ru.itis.utils;

import ru.itis.dto.auth.EncryptedPasswordDTO;
import ru.itis.exceptions.PasswordDecryptionException;
import ru.itis.exceptions.PasswordEncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;


@Service
public class PasswordCryptoService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final String secretKey;

    public PasswordCryptoService(
            @Value("${app.security.pending-registration.secret-key}") String secretKey
    ) {
        this.secretKey = secretKey;
    }

    public EncryptedPasswordDTO encrypt(String rawValue) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] encryptedBytes = cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8));

            return new EncryptedPasswordDTO(
                    Base64.getEncoder().encodeToString(encryptedBytes),
                    Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            throw new PasswordEncryptionException("Ошибка при шифровании пароля", e.getCause());
        }
    }

    public String decrypt(String cipherText, String ivBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PasswordDecryptionException("Ошибка при расшифровке пароля", e.getCause());
        }
    }
}

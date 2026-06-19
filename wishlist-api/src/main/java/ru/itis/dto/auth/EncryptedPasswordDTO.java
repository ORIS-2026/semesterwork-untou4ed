package ru.itis.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EncryptedPasswordDTO {
    String encryptedPassword;
    String passwordSalt;
}

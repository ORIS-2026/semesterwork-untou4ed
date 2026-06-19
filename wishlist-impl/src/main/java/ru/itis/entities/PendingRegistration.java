package ru.itis.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingRegistration implements Serializable {

    private String registrationId;
    private String name;
    private String surname;
    private String username;
    private String phoneNumber;
    private String encryptedPassword;
    private String passwordSalt;
    private Instant createdAt;
}

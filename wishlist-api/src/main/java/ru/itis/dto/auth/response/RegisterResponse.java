package ru.itis.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterResponse {
    private String registrationId;
    private String phoneNumber;
}

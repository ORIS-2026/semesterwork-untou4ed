package ru.itis.dto.auth.request;

import lombok.Data;

@Data
public class VerifyRequest {
    private String registrationId;
    private String code;
}

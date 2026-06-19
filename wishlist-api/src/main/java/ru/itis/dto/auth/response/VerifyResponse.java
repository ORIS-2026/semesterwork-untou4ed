package ru.itis.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyResponse {
    private boolean verified;
}
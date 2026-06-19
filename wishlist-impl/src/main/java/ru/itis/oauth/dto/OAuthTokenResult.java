package ru.itis.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthTokenResult {
    private String accessToken;
    private String refreshToken;
}

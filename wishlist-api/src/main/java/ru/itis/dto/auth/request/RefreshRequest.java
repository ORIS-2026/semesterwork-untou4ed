package ru.itis.dto.auth.request;

import lombok.Getter;

@Getter
public class RefreshRequest {
    private String refreshToken;
}
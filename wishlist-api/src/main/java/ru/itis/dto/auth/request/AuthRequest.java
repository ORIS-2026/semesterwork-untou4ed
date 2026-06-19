package ru.itis.dto.auth.request;

import lombok.Getter;

@Getter
public class AuthRequest {
    private String username;
    private String password;
}

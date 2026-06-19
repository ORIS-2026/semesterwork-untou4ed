package ru.itis.services;

import ru.itis.dto.auth.request.AuthRequest;
import ru.itis.dto.auth.request.CodeRequest;
import ru.itis.dto.auth.request.RegisterRequest;
import ru.itis.dto.auth.request.VerifyRequest;
import ru.itis.dto.auth.response.LoginResponse;
import ru.itis.dto.auth.response.RegisterResponse;
import ru.itis.dto.auth.response.VerifyResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest, HttpServletRequest request);

    VerifyResponse verifyNumber(VerifyRequest request);

    void sendCode(CodeRequest codeRequest, HttpServletRequest request);

    LoginResponse login(AuthRequest request);
}

package ru.itis.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itis.api.AuthApi;
import ru.itis.dto.auth.request.CodeRequest;
import ru.itis.dto.auth.request.RegisterRequest;
import ru.itis.dto.auth.request.VerifyRequest;
import ru.itis.dto.auth.response.RegisterResponse;
import ru.itis.dto.auth.response.VerifyResponse;
import ru.itis.services.AuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest registerRequest,
                                                     HttpServletRequest request) {
        log.info("Новый запрос на регистрацию");
        return ResponseEntity.status(201).body(authService.register(registerRequest, request));
    }

    @Override
    public ResponseEntity<VerifyResponse> verifyNumber(@RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verifyNumber(request));
    }

    @Override
    public ResponseEntity<?> sendCode(
            @RequestBody CodeRequest codeRequest,
            HttpServletRequest request
    ) {
        authService.sendCode(codeRequest, request);
        return ResponseEntity.ok().build();
    }
}
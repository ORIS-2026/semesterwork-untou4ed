package ru.itis.api;

import ru.itis.dto.auth.request.CodeRequest;
import ru.itis.dto.auth.request.RegisterRequest;
import ru.itis.dto.auth.request.VerifyRequest;
import ru.itis.dto.auth.response.RegisterResponse;
import ru.itis.dto.auth.response.VerifyResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/api/v1/auth")
public interface AuthApi {
    @PostMapping("/register")
    ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest registerRequest,
                                              @Parameter(hidden = true) HttpServletRequest request);

    @PostMapping("/verify")
    ResponseEntity<VerifyResponse> verifyNumber(@RequestBody VerifyRequest request);

    @PostMapping("/send-code")
    ResponseEntity<?> sendCode(@RequestBody CodeRequest codeRequest,
                               @Parameter(hidden = true) HttpServletRequest request);
}

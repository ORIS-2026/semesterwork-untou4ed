package ru.itis.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.dto.user.response.UserResponse;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/subscriptions")
@RestController
public interface SubscriptionApi {

    @GetMapping
    List<UserResponse> getMySubscriptions(@AuthenticationPrincipal Jwt jwt);

    @GetMapping("/followers")
    List<UserResponse> getMyFollowers(@AuthenticationPrincipal Jwt jwt);

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void subscribe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId
    );

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unsubscribe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId
    );
}

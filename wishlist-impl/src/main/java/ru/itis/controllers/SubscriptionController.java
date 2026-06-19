package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.api.SubscriptionApi;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.services.subscriptionService.SubscriptionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionApi {

    private final SubscriptionService subscriptionService;

    @Override
    public List<UserResponse> getMySubscriptions(Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        return subscriptionService.getMySubscriptions(currentUserId);
    }

    @Override
    public void subscribe(Jwt jwt, UUID userId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        subscriptionService.subscribe(currentUserId, userId);
    }

    @Override
    public void unsubscribe(Jwt jwt, UUID userId) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        subscriptionService.unsubscribe(currentUserId, userId);
    }

    @Override
    public List<UserResponse> getMyFollowers(Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        return subscriptionService.getMyFollowers(currentUserId);
    }
}

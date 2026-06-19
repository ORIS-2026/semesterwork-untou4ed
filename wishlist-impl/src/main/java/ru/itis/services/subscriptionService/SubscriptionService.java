package ru.itis.services.subscriptionService;

import ru.itis.dto.user.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    void subscribe(UUID followerId, UUID followingId);

    void unsubscribe(UUID followerId, UUID followingId);

    List<UserResponse> getMySubscriptions(UUID followerId);

    List<UserResponse> getMyFollowers(UUID userId);
}

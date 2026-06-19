package ru.itis.services.notificationService.redis.email;

import ru.itis.dto.PendingEmailChange;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisEmailVerificationStorageService implements EmailVerificationStorageService {

    private static final String PREFIX = "email-verify:";
    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(UUID userId, PendingEmailChange pendingEmailChange) {
        redisTemplate.opsForValue().set(key(userId), pendingEmailChange, TTL);
    }

    @Override
    public Optional<PendingEmailChange> find(UUID userId) {
        Object value = redisTemplate.opsForValue().get(key(userId));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of((PendingEmailChange) value);
    }

    @Override
    public void delete(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(UUID userId) {
        return PREFIX + userId;
    }
}

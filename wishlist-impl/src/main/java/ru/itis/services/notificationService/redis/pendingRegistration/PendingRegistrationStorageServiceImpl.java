package ru.itis.services.notificationService.redis.pendingRegistration;

import ru.itis.entities.PendingRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PendingRegistrationStorageServiceImpl implements PendingRegistrationStorageService {

    private static final String REG_PREFIX = "reg:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(PendingRegistration registration, Duration ttl) {
        String key = buildKey(registration.getRegistrationId());
        redisTemplate.opsForValue().set(key, registration, ttl);
    }

    @Override
    public Optional<PendingRegistration> findById(String registrationId) {
        String key = buildKey(registrationId);
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of((PendingRegistration) value);
    }

    @Override
    public void delete(String registrationId) {
        redisTemplate.delete(buildKey(registrationId));
    }

    private String buildKey(String registrationId) {
        return REG_PREFIX + registrationId;
    }
}
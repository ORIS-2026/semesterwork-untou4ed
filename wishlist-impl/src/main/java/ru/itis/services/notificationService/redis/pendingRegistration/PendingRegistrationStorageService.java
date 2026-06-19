package ru.itis.services.notificationService.redis.pendingRegistration;

import ru.itis.entities.PendingRegistration;

import java.time.Duration;
import java.util.Optional;

public interface PendingRegistrationStorageService {
    void save(PendingRegistration registration, Duration ttl);
    Optional<PendingRegistration> findById(String registrationId);
    void delete(String registrationId);
}
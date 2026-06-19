package ru.itis.services.notificationService.redis.email;

import ru.itis.dto.PendingEmailChange;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationStorageService {
    void save(UUID userId, PendingEmailChange pendingEmailChange);

    Optional<PendingEmailChange> find(UUID userId);

    void delete(UUID userId);
}

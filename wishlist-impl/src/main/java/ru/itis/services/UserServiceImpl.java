package ru.itis.services;

import ru.itis.dto.PendingEmailChange;
import ru.itis.dto.user.request.UpdateUserProfileRequest;
import ru.itis.dto.user.response.AvatarUploadUrlResponse;
import ru.itis.dto.user.response.UserAdminResponse;
import ru.itis.dto.user.response.UserMeResponse;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.entities.User;
import ru.itis.exceptions.EmailAlreadyTakenException;
import ru.itis.exceptions.UserNotFoundException;
import ru.itis.exceptions.UsernameAlreadyTakenException;
import ru.itis.mappers.UserMapper;
import ru.itis.properties.MinioProperties;
import ru.itis.repositories.UserRepository;
import ru.itis.services.keycloak.KeycloakAdminService;
import ru.itis.services.minio.MinioStorageService;
import ru.itis.services.notificationService.redis.email.EmailVerificationStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;
    private final MinioStorageService minioStorageService;
    private final MinioProperties minioProperties;
    private final KeycloakAdminService keycloakAdminService;
    private final EmailVerificationStorageService emailVerificationStorageService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Cacheable(value = "usersMe", key = "#userId")
    public UserMeResponse getMe(UUID userId) {
        User user = repository.findByIdAndDeletedAtIsNull(userId).orElseThrow(() -> {
            log.warn("Не удалось найти пользователя по userId {}", userId);
            return new UserNotFoundException("Пользователь не найден");
        });
        return UserMapper.INSTANCE.toUserMeResponse(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersById", key = "#userId"),
            @CacheEvict(value = "usersMe", key = "#userId")
    })
    public UserAdminResponse blockUser(UUID userId) {
        User user = repository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!user.getEnabled()) {
            throw new IllegalStateException("Пользователь уже заблокирован");
        }

        user.setEnabled(false);

        log.info("Блокировка пользователя {}", userId);

        return userMapper.toUserAdminResponse(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersById", key = "#userId"),
            @CacheEvict(value = "usersMe", key = "#userId")
    })
    public UserAdminResponse unblockUser(UUID userId) {
        User user = repository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (user.getEnabled()) {
            throw new IllegalStateException("Пользователь уже активен");
        }

        user.setEnabled(true);

        log.info("Разблокировка пользователя {}", userId);

        return userMapper.toUserAdminResponse(user);
    }

    @Override
    public Page<UserAdminResponse> findAllUsers(int page, int size, Boolean enabled, boolean includeDeleted) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAllWithFilters(enabled, includeDeleted, pageable)
                .map(userMapper::toUserAdminResponse);
    }

    @Override
    @Cacheable(value = "usersById", key = "#userId")
    public UserResponse findById(String userId) {
        User user = repository.findByIdAndDeletedAtIsNull(UUID.fromString(userId)).orElseThrow(() -> {
            log.warn("Не удалось найти пользователя по id {}", userId);
            return new UserNotFoundException("Не удалось найти пользователя.");
        });
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = "usersByUsername", key = "#username")
    public UserResponse findByUsername(String username) {
        User user = repository.findByUsernameAndDeletedAtIsNull(username).orElseThrow(() -> {
            log.warn("Не удалось найти пользователя по username {}", username);
            return new UserNotFoundException("Не удалось найти пользователя.");
        });
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersById", key = "#result.id"),
            @CacheEvict(value = "usersByUsername", key = "#result.username"),
            @CacheEvict(value = "usersMe", key = "#userId")
    })
    public UserResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        User user = repository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("Не удалось найти пользователя"));

        if (request.name() != null) {
            user.setName(request.name().trim());
        }

        if (request.surname() != null) {
            user.setSurname(request.surname().trim());
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersById", key = "#userId"),
            @CacheEvict(value = "usersByUsername", key = "#result.username"),
            @CacheEvict(value = "usersMe", key = "#userId")
    })
    public UserMeResponse updateUsername(UUID userId, String username) {
        if (repository.existsUserByUsernameAndDeletedAtIsNull(username)) {
            throw new UsernameAlreadyTakenException("Username уже занят");
        }

        User user = repository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setUsername(username);

        keycloakAdminService.updateUsername(userId.toString(), username);

        log.info("Username пользователя {} изменён на {}", userId, username);

        return userMapper.toUserMeResponse(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersById", key = "#userId"),
            @CacheEvict(value = "usersMe", key = "#userId")
    })
    public AvatarUploadUrlResponse generateAvatarUploadUrl(UUID userId) {
        User user = repository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("Не удалось найти пользователя"));

        String objectKey = minioStorageService.buildAvatarObjectKey(userId);
        Map<String, String> formData = minioStorageService.generateUploadFormData(
                objectKey,
                minioProperties.getAvatarUploadTtlMinutes(),
                minioProperties.getAvatarMaxSizeBytes());

        user.setAvatarUrl(objectKey);
        repository.save(user);

        log.info("Создан upload form data для пользователя {}, key: {}", userId, objectKey);

        String uploadUrl = minioProperties.getEndpoint() + "/" + minioProperties.getBucket();
        return new AvatarUploadUrlResponse(uploadUrl, formData, minioProperties.getAvatarUploadTtlMinutes());
    }

    @Override
    public String getAvatarDownloadUrl(UUID userId) {
        User user = repository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("Не удалось найти пользователя"));

        if (user.getAvatarUrl() == null) {
            throw new UserNotFoundException("Аватар не найден");
        }

        return minioStorageService.generateDownloadPresignedUrl(
                user.getAvatarUrl(), minioProperties.getAvatarDownloadTtlMinutes());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersMe",        key = "#userId"),
            @CacheEvict(value = "usersById",       key = "#userId"),
            @CacheEvict(value = "usersByUsername", allEntries = true)
    })
    public void deleteMe(UUID userId) {
        User user = repository.findByIdAndDeletedAtIsNull(userId).orElseThrow(() -> {
            log.warn("Попытка удаления пользователя, которого нет среди активных, userId {}", userId);
            return new UserNotFoundException("Не удалось найти пользователя");
        });

        user.setDeletedAt(Instant.now());
        log.info("Soft-delete пользователя {} в БД", user.getId());

        // Отключаем пользователя в Keycloak — без этого он может продолжать входить
        keycloakAdminService.disableUser(userId.toString());
        log.info("Пользователь {} отключён в Keycloak", user.getId());
    }

    @Override
    public void requestEmailChange(UUID userId, String email) {
        if (repository.existsUserByEmailAndDeletedAtIsNull(email)) {
            throw new EmailAlreadyTakenException("Email уже используется");
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        emailVerificationStorageService.save(userId, new PendingEmailChange(email, code));

        log.info("Запрос на смену email для пользователя {}, письмо отправлено на {}", userId, email);
        log.info("Код для подтверждения почты {} {}", code, email);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "usersMe", key = "#userId"),
            @CacheEvict(value = "usersById", key = "#userId")
    })
    public UserMeResponse confirmEmailChange(UUID userId, String code) {
        PendingEmailChange pending = emailVerificationStorageService.find(userId)
                .orElseThrow(() -> new IllegalStateException("Запрос на смену email не найден или истёк"));

        if (!pending.getCode().equals(code)) {
            throw new IllegalArgumentException("Неверный код подтверждения");
        }

        if (repository.existsUserByEmailAndDeletedAtIsNull(pending.getEmail())) {
            throw new EmailAlreadyTakenException("Email уже занят другим пользователем");
        }

        User user = repository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setEmail(pending.getEmail());

        keycloakAdminService.updateEmail(userId.toString(), pending.getEmail());

        emailVerificationStorageService.delete(userId);

        log.info("Email успешно подтверждён для пользователя {}", userId);

        return userMapper.toUserMeResponse(user);
    }

}

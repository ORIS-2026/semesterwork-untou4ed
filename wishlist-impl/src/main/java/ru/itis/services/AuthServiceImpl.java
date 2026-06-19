package ru.itis.services;

import ru.itis.dto.auth.EncryptedPasswordDTO;
import ru.itis.dto.auth.request.AuthRequest;
import ru.itis.dto.auth.request.CodeRequest;
import ru.itis.dto.auth.request.RegisterRequest;
import ru.itis.dto.auth.request.VerifyRequest;
import ru.itis.dto.auth.response.LoginResponse;
import ru.itis.dto.auth.response.RegisterResponse;
import ru.itis.dto.auth.response.VerifyResponse;
import ru.itis.entities.PendingRegistration;
import ru.itis.entities.Role;
import ru.itis.entities.User;
import ru.itis.exceptions.*;
import ru.itis.properties.CodeProperty;
import ru.itis.repositories.UserRepository;
import ru.itis.services.keycloak.KeycloakAdminService;
import ru.itis.services.wishlistService.WishlistService;
import ru.itis.api.TelegramGatewayApi;
import ru.itis.services.notificationService.redis.pendingRegistration.PendingRegistrationStorageService;
import ru.itis.services.notificationService.redis.smsCode.RateLimiterService;
import ru.itis.services.notificationService.redis.smsCode.SmsCodeStorageService;
import ru.itis.services.notificationService.redis.smsCode.TokenBucketLuaScript;
import ru.itis.utils.IpUtil;
import ru.itis.utils.PasswordCryptoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;


    private final RateLimiterService rateLimiterService;
    private final SecureRandom random = new SecureRandom();
    private final TokenBucketLuaScript tokenBucketLuaScript;

    private final SmsCodeStorageService smsCodeStorageService;
    private final CodeProperty codeProperty;

    private final KeycloakAdminService keycloakAdminService;
    private final WishlistService wishlistService;


    private final PendingRegistrationStorageService pendingRegistrationStorageService;
    private final PasswordCryptoService passwordCryptoService;

    private final TelegramGatewayApi telegramGatewayApi;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest, HttpServletRequest request) {

        if (userRepository.existsUserByUsernameAndDeletedAtIsNull(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Имя пользователя занято");
        }

        String phoneNumber = checkPhoneNumber(registerRequest.getNumber());

        if (userRepository.existsUserByPhoneNumberAndDeletedAtIsNull(phoneNumber)) {
            throw new UserAlreadyExistsException("Номер телефона уже используется");
        }

        String registrationId = UUID.randomUUID().toString();

        EncryptedPasswordDTO encryptedPasswordDTO = passwordCryptoService.encrypt(registerRequest.getPassword());

        PendingRegistration pendingRegistration = PendingRegistration.builder()
                .registrationId(registrationId)
                .name(registerRequest.getName())
                .surname(registerRequest.getSurname())
                .username(registerRequest.getUsername())
                .phoneNumber(phoneNumber)
                .encryptedPassword(encryptedPasswordDTO.getEncryptedPassword())
                .passwordSalt(encryptedPasswordDTO.getPasswordSalt())
                .createdAt(Instant.now())
                .build();

        pendingRegistrationStorageService.save(pendingRegistration,
                Duration.ofMinutes(codeProperty.getTotalTime() + codeProperty.getSingleSmsCooldown()));

        log.info("Добавлена регистрация {}, необходимо подтвердить номер", pendingRegistration);

        sendCode(new CodeRequest(registrationId), request);
        return new RegisterResponse(registrationId, phoneNumber);
    }

    @Override
    public void sendCode(CodeRequest codeRequest, HttpServletRequest request) {
        String ip = IpUtil.getClientIp(request);

        boolean ipAllowed = rateLimiterService.allow(
                "ip:" + ip,
                tokenBucketLuaScript.getScript()
        );
        if (!ipAllowed) {
            throw new TooManyAttemptsException("Слишком много запросов с IP");
        }

        PendingRegistration pendingRegistration = pendingRegistrationStorageService.findById(codeRequest.getRegistrationId())
                .orElseThrow(() -> new RegistrationNotFoundException("Регистрация не существовала или истекла"));

        String phoneNumber = checkPhoneNumber(pendingRegistration.getPhoneNumber());

        boolean phoneAllowed = rateLimiterService.allow(
                "phone:" + phoneNumber,
                tokenBucketLuaScript.getScript()
        );
        if (!phoneAllowed) {
            throw new TooManyAttemptsException("Слишком много запросов для номера");
        }

        int code = generate5digitCode();

        String codeStr = String.valueOf(code);
        int ttlSeconds = codeProperty.getExpiration() * 60;

        smsCodeStorageService.deleteCode(phoneNumber);

        log.info("Сохраняем новый код для номера {} в redis", phoneNumber);
        smsCodeStorageService.saveCode(phoneNumber, codeStr, Duration.ofMinutes(codeProperty.getExpiration()));

        // обновляем количество попыток ввода для нового кода
        smsCodeStorageService.deleteAttempts(phoneNumber);

        // отправляем код через Telegram Gateway
        telegramGatewayApi.sendCode(phoneNumber, codeStr, ttlSeconds);
    }

    @Override
    public LoginResponse login(@NotNull AuthRequest request) {
        return new LoginResponse();
    }

    private String checkPhoneNumber(String number) {
        String phoneNumber = number.replaceAll("[\\s\\-]", "");

        phoneNumber = phoneNumber.startsWith("8") ? "+7%s".formatted(phoneNumber.substring(1))
                : phoneNumber;

        if (phoneNumber.startsWith("7")) {
            phoneNumber = "+%s".formatted(phoneNumber);
        }

        return phoneNumber;
    }

    @Transactional
    @Override
    public VerifyResponse verifyNumber(VerifyRequest verifyRequest) {

        PendingRegistration pendingRegistration = pendingRegistrationStorageService.findById(verifyRequest.getRegistrationId())
                .orElseThrow(() -> {
                    log.warn("Сессия регистрации {} не найдена или истекла", verifyRequest.getRegistrationId());
                    return new RegistrationNotFoundException("Регистрация не существовала или истекла");
                });

        String phoneNumber = checkPhoneNumber(pendingRegistration.getPhoneNumber());
        pendingRegistration.setPhoneNumber(phoneNumber);

        // сколько на данный момент попыток ввести код
        Long attempts = smsCodeStorageService.incrementAttempts(
                phoneNumber,
                Duration.ofMinutes(codeProperty.getExpiration())
        );

        //5 (n) попыток ввести код, иначе ждет пока не истечет или не запросит новый
        if (attempts > codeProperty.getEnterSingleCodeAttempts()) {
            log.warn("Слишком много попыток ввода для номера {}", phoneNumber);
            throw new TooManyAttemptsException("Слишком много попыток");
        }

        String code = smsCodeStorageService.getCode(phoneNumber);

        if (code == null) {
            log.warn("Код для номера {} истёк или не найден", phoneNumber);
            throw new InvalidCodeException("Код истёк или не существует");
        }


        if (!code.equals(verifyRequest.getCode())) {
            log.warn("Введён неверный код для номера {}", phoneNumber);
            throw new InvalidCodeException("Неверный код");
        }

        // прошли все проверки, значит код верный, можем создавать пользователя
        log.info("Введен верный код для номера {}", phoneNumber);

        createUser(pendingRegistration);

        smsCodeStorageService.deleteCode(phoneNumber);
        smsCodeStorageService.deleteAttempts(phoneNumber);

        return new VerifyResponse(true);
    }

    private void createUser(PendingRegistration pendingRegistration) {
        UUID keycloakUserId = keycloakAdminService.createUser(
                pendingRegistration.getUsername(),
                pendingRegistration.getPhoneNumber(),
                passwordCryptoService.decrypt(pendingRegistration.getEncryptedPassword(),
                        pendingRegistration.getPasswordSalt())
        );
        log.info("Пользователь с регистрацией {} добавлен в keycloak", pendingRegistration.getRegistrationId());

        User user = User.builder()
                .id(keycloakUserId)
                .name(pendingRegistration.getName())
                .surname(pendingRegistration.getSurname())
                .username(pendingRegistration.getUsername())
                .phoneNumber(pendingRegistration.getPhoneNumber())
                .email(null)
                .avatarUrl(null)
                .enabled(true)
                .createdAt(pendingRegistration.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        keycloakAdminService.assignRealmRole(keycloakUserId, Role.USER.getValue());
        log.info("Пользователь с регистрацией {} выдана роль {}", pendingRegistration.getRegistrationId(), Role.USER.getValue());

        user.setEnabled(true);
        log.info("Пользователь {} активирован", user);

        userRepository.saveAndFlush(user);
        log.info("Пользователь {} сохранен", user);

        wishlistService.createUserWishlist(user);
        log.info("Для пользователя {} создан вишлист", user);

        pendingRegistrationStorageService.delete(pendingRegistration.getRegistrationId());

    }

    private int generate5digitCode() {
        return 10000 + random.nextInt(90000);
    }
}

package ru.itis.services;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.api.TelegramGatewayApi;
import ru.itis.dto.auth.EncryptedPasswordDTO;
import ru.itis.dto.auth.request.CodeRequest;
import ru.itis.dto.auth.request.RegisterRequest;
import ru.itis.dto.auth.request.VerifyRequest;
import ru.itis.dto.auth.response.RegisterResponse;
import ru.itis.dto.auth.response.VerifyResponse;
import ru.itis.entities.PendingRegistration;
import ru.itis.exceptions.InvalidCodeException;
import ru.itis.exceptions.RegistrationNotFoundException;
import ru.itis.exceptions.TooManyAttemptsException;
import ru.itis.exceptions.UserAlreadyExistsException;
import ru.itis.properties.CodeProperty;
import ru.itis.repositories.UserRepository;
import ru.itis.services.keycloak.KeycloakAdminService;
import ru.itis.services.notificationService.redis.pendingRegistration.PendingRegistrationStorageService;
import ru.itis.services.notificationService.redis.smsCode.RateLimiterService;
import ru.itis.services.notificationService.redis.smsCode.SmsCodeStorageService;
import ru.itis.services.notificationService.redis.smsCode.TokenBucketLuaScript;
import ru.itis.services.wishlistService.WishlistService;
import ru.itis.utils.PasswordCryptoService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RateLimiterService rateLimiterService;
    @Mock TokenBucketLuaScript tokenBucketLuaScript;
    @Mock SmsCodeStorageService smsCodeStorageService;
    @Mock CodeProperty codeProperty;
    @Mock KeycloakAdminService keycloakAdminService;
    @Mock WishlistService wishlistService;
    @Mock PendingRegistrationStorageService pendingRegistrationStorageService;
    @Mock PasswordCryptoService passwordCryptoService;
    @Mock TelegramGatewayApi telegramGatewayApi;
    @InjectMocks AuthServiceImpl authService;

    private HttpServletRequest mockRequest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Forwarded-For")).thenReturn(null);
        when(req.getRemoteAddr()).thenReturn("127.0.0.1");
        return req;
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .name("Иван")
                .surname("Иванов")
                .username("ivan123")
                .password("Password1")
                .passwordRepeat("Password1")
                .number("+79001234567")
                .build();
    }

    private PendingRegistration pendingRegistration(String phone) {
        return PendingRegistration.builder()
                .registrationId(UUID.randomUUID().toString())
                .name("Иван")
                .surname("Иванов")
                .username("ivan123")
                .phoneNumber(phone)
                .encryptedPassword("enc")
                .passwordSalt("salt")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void register_usernameTaken_throws() {
        when(userRepository.existsUserByUsernameAndDeletedAtIsNull("ivan123")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest(), mockRequest()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("занято");
    }

    @Test
    void register_phoneTaken_throws() {
        when(userRepository.existsUserByUsernameAndDeletedAtIsNull("ivan123")).thenReturn(false);
        when(userRepository.existsUserByPhoneNumberAndDeletedAtIsNull("+79001234567")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest(), mockRequest()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Номер");
    }

    @Test
    void register_success_returnsPendingRegistration() {
        HttpServletRequest req = mockRequest();
        RegisterRequest registerReq = registerRequest();

        when(userRepository.existsUserByUsernameAndDeletedAtIsNull("ivan123")).thenReturn(false);
        when(userRepository.existsUserByPhoneNumberAndDeletedAtIsNull("+79001234567")).thenReturn(false);
        when(passwordCryptoService.encrypt(any())).thenReturn(new EncryptedPasswordDTO("enc", "salt"));
        when(codeProperty.getTotalTime()).thenReturn(10);
        when(codeProperty.getSingleSmsCooldown()).thenReturn(2);
        when(codeProperty.getExpiration()).thenReturn(5);
        when(rateLimiterService.allow(anyString(), any())).thenReturn(true);
        when(pendingRegistrationStorageService.findById(anyString()))
                .thenAnswer(inv -> Optional.of(pendingRegistration("+79001234567")));
        when(smsCodeStorageService.incrementAttempts(anyString(), any())).thenReturn(1L);

        RegisterResponse result = authService.register(registerReq, req);

        assertThat(result).isNotNull();
        assertThat(result.getPhoneNumber()).isEqualTo("+79001234567");
        verify(pendingRegistrationStorageService).save(any(PendingRegistration.class), any());
    }

    @Test
    void sendCode_ipRateLimited_throws() {
        HttpServletRequest req = mockRequest();
        when(rateLimiterService.allow(eq("ip:127.0.0.1"), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.sendCode(new CodeRequest("reg-id"), req))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasMessageContaining("IP");
    }

    @Test
    void sendCode_registrationNotFound_throws() {
        HttpServletRequest req = mockRequest();
        String regId = UUID.randomUUID().toString();

        when(rateLimiterService.allow(eq("ip:127.0.0.1"), any())).thenReturn(true);
        when(pendingRegistrationStorageService.findById(regId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.sendCode(new CodeRequest(regId), req))
                .isInstanceOf(RegistrationNotFoundException.class);
    }

    @Test
    void sendCode_phoneRateLimited_throws() {
        HttpServletRequest req = mockRequest();
        String regId = UUID.randomUUID().toString();
        PendingRegistration pending = pendingRegistration("+79001234567");
        pending.setRegistrationId(regId);

        when(rateLimiterService.allow(eq("ip:127.0.0.1"), any())).thenReturn(true);
        when(pendingRegistrationStorageService.findById(regId)).thenReturn(Optional.of(pending));
        when(rateLimiterService.allow(eq("phone:+79001234567"), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.sendCode(new CodeRequest(regId), req))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasMessageContaining("номера");
    }

    @Test
    void sendCode_success_saveAndSendCode() {
        HttpServletRequest req = mockRequest();
        String regId = UUID.randomUUID().toString();
        PendingRegistration pending = pendingRegistration("+79001234567");
        pending.setRegistrationId(regId);

        when(rateLimiterService.allow(anyString(), any())).thenReturn(true);
        when(pendingRegistrationStorageService.findById(regId)).thenReturn(Optional.of(pending));
        when(codeProperty.getExpiration()).thenReturn(5);

        authService.sendCode(new CodeRequest(regId), req);

        verify(smsCodeStorageService).deleteCode("+79001234567");
        verify(smsCodeStorageService).saveCode(eq("+79001234567"), anyString(), any());
        verify(telegramGatewayApi).sendCode(eq("+79001234567"), anyString(), anyInt());
    }

    @Test
    void verifyNumber_registrationNotFound_throws() {
        VerifyRequest req = new VerifyRequest();
        req.setRegistrationId("unknown-id");
        req.setCode("12345");

        when(pendingRegistrationStorageService.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyNumber(req))
                .isInstanceOf(RegistrationNotFoundException.class);
    }

    @Test
    void verifyNumber_tooManyAttempts_throws() {
        String regId = UUID.randomUUID().toString();
        PendingRegistration pending = pendingRegistration("+79001234567");
        pending.setRegistrationId(regId);

        VerifyRequest req = new VerifyRequest();
        req.setRegistrationId(regId);
        req.setCode("12345");

        when(pendingRegistrationStorageService.findById(regId)).thenReturn(Optional.of(pending));
        when(codeProperty.getEnterSingleCodeAttempts()).thenReturn(5);
        when(smsCodeStorageService.incrementAttempts(eq("+79001234567"), any())).thenReturn(6L);

        assertThatThrownBy(() -> authService.verifyNumber(req))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasMessageContaining("Слишком много");
    }

    @Test
    void verifyNumber_codeExpired_throws() {
        String regId = UUID.randomUUID().toString();
        PendingRegistration pending = pendingRegistration("+79001234567");
        pending.setRegistrationId(regId);

        VerifyRequest req = new VerifyRequest();
        req.setRegistrationId(regId);
        req.setCode("12345");

        when(pendingRegistrationStorageService.findById(regId)).thenReturn(Optional.of(pending));
        when(codeProperty.getEnterSingleCodeAttempts()).thenReturn(5);
        when(smsCodeStorageService.incrementAttempts(eq("+79001234567"), any())).thenReturn(1L);
        when(smsCodeStorageService.getCode("+79001234567")).thenReturn(null);

        assertThatThrownBy(() -> authService.verifyNumber(req))
                .isInstanceOf(InvalidCodeException.class)
                .hasMessageContaining("истёк");
    }

    @Test
    void verifyNumber_wrongCode_throws() {
        String regId = UUID.randomUUID().toString();
        PendingRegistration pending = pendingRegistration("+79001234567");
        pending.setRegistrationId(regId);

        VerifyRequest req = new VerifyRequest();
        req.setRegistrationId(regId);
        req.setCode("00000");

        when(pendingRegistrationStorageService.findById(regId)).thenReturn(Optional.of(pending));
        when(codeProperty.getEnterSingleCodeAttempts()).thenReturn(5);
        when(smsCodeStorageService.incrementAttempts(eq("+79001234567"), any())).thenReturn(1L);
        when(smsCodeStorageService.getCode("+79001234567")).thenReturn("12345");

        assertThatThrownBy(() -> authService.verifyNumber(req))
                .isInstanceOf(InvalidCodeException.class)
                .hasMessageContaining("Неверный");
    }

    @Test
    void verifyNumber_correctCode_createsUser() {
        String regId = UUID.randomUUID().toString();
        PendingRegistration pending = pendingRegistration("+79001234567");
        pending.setRegistrationId(regId);
        UUID keycloakId = UUID.randomUUID();

        VerifyRequest req = new VerifyRequest();
        req.setRegistrationId(regId);
        req.setCode("12345");

        when(pendingRegistrationStorageService.findById(regId)).thenReturn(Optional.of(pending));
        when(codeProperty.getEnterSingleCodeAttempts()).thenReturn(5);
        when(smsCodeStorageService.incrementAttempts(eq("+79001234567"), any())).thenReturn(1L);
        when(smsCodeStorageService.getCode("+79001234567")).thenReturn("12345");
        when(keycloakAdminService.createUser(anyString(), anyString(), anyString())).thenReturn(keycloakId);
        when(passwordCryptoService.decrypt(anyString(), anyString())).thenReturn("Password1");

        VerifyResponse result = authService.verifyNumber(req);

        assertThat(result.isVerified()).isTrue();
        verify(userRepository).saveAndFlush(any());
        verify(keycloakAdminService).assignRealmRole(eq(keycloakId), anyString());
        verify(wishlistService).createUserWishlist(any());
        verify(smsCodeStorageService).deleteCode("+79001234567");
        verify(smsCodeStorageService).deleteAttempts("+79001234567");
        verify(pendingRegistrationStorageService).delete(regId);
    }
}

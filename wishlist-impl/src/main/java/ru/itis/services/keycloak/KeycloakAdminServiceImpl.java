package ru.itis.services.keycloak;

import ru.itis.dto.KeycloakUserRepresentation;
import ru.itis.feignClients.KeycloakAdminClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminServiceImpl implements KeycloakAdminService {
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public UUID createUser(
            String username,
            String phoneNumber,
            String password
    ) {
        Map<String, Object> user = Map.of(
                "username", username,
                "enabled", true,
                "attributes", Map.of(
                        "phone_number", List.of(phoneNumber),
                        "phone_verified", List.of("true")
                )
        );

        ResponseEntity<Void> response = keycloakAdminClient.createUser(user);

        UUID userId = extractUserId(response);
        log.info("Создан пользователь в Keycloak: {}", userId);

        setPassword(userId, password);

        return userId;
    }

    @Override
    public void disableUser(String userId) {
        KeycloakUserRepresentation keycloakUser = keycloakAdminClient.getUser(userId);
        keycloakUser.setEnabled(false);
        log.info("Отключение пользователя в Keycloak {}", userId);
        keycloakAdminClient.updateUser(userId, keycloakUser);
        log.info("Пользователь отключён в Keycloak {}", userId);
    }

    @Override
    public void enableUser(String userId) {
        KeycloakUserRepresentation keycloakUser = keycloakAdminClient.getUser(userId);
        keycloakUser.setEnabled(true);
        log.info("Включение пользователя в Keycloak {}", userId);
        keycloakAdminClient.updateUser(userId, keycloakUser);
        log.info("Пользователь включён в Keycloak {}", userId);
    }

    @Override
    public void updateEmail(String userId, String email) {
        KeycloakUserRepresentation keycloakUser = keycloakAdminClient.getUser(userId);
        keycloakUser.setEmail(email);
        keycloakUser.setEmailVerified(true);
        keycloakAdminClient.updateUser(userId, keycloakUser);
        log.info("Email обновлён и подтверждён в Keycloak для пользователя {}", userId);
    }

    @Override
    public void updateUsername(String userId, String username) {
        KeycloakUserRepresentation keycloakUser = keycloakAdminClient.getUser(userId);
        keycloakUser.setUsername(username);
        keycloakAdminClient.updateUser(userId, keycloakUser);
        log.info("Username обновлён в Keycloak для пользователя {}: {}", userId, username);
    }

    private void setPassword(UUID userId, String password) {
        Map<String, Object> passwordRequest = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );

        keycloakAdminClient.setPassword(userId, passwordRequest);

        log.info("Пароль установлен для пользователя {}", userId);
    }

    private UUID extractUserId(ResponseEntity<Void> response) {
        URI location = response.getHeaders().getLocation();

        if (location == null) {
            throw new RuntimeException("Не удалось получить userId из Keycloak");
        }

        String path = location.getPath();
        return UUID.fromString(path.substring(path.lastIndexOf("/") + 1));
    }

    @Override
    public void assignRealmRole(UUID userId, String roleName) {
        Map<String, Object> role = keycloakAdminClient.getRealmRole(roleName);

        keycloakAdminClient.assignRealmRole(
                userId,
                List.of(role)
        );

        log.info("Назначена роль {} пользователю {}", roleName, userId);
    }
}

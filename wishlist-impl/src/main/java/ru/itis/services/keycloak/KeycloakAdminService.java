package ru.itis.services.keycloak;

import java.util.UUID;

public interface KeycloakAdminService {
    UUID createUser(
            String username,
            String phoneNumber,
            String password
    );

    void disableUser(String userId);

    void enableUser(String userId);

    void updateEmail(String userId, String email);

    void updateUsername(String userId, String username);

    void assignRealmRole(UUID userId, String roleName);
}

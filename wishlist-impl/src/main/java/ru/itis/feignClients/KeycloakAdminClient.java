package ru.itis.feignClients;

import ru.itis.config.FeignAuthConfig;
import ru.itis.dto.KeycloakUserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "keycloak",
        contextId = "keycloakAdminClient",
        url = "${keycloak.url}",
        configuration = FeignAuthConfig.class)
public interface KeycloakAdminClient {

    @PostMapping("/admin/realms/${keycloak.realm}/users")
    ResponseEntity<Void> createUser(@RequestBody Map<String, Object> user);

    @PutMapping("/admin/realms/${keycloak.realm}/users/{userId}/reset-password")
    void setPassword(
            @PathVariable("userId") UUID userId,
            @RequestBody Map<String, Object> password
    );

    @GetMapping("/admin/realms/${keycloak.realm}/roles/{roleName}")
    Map<String, Object> getRealmRole(@PathVariable("roleName") String roleName);

    @PostMapping("/admin/realms/${keycloak.realm}/users/{userId}/role-mappings/realm")
    void assignRealmRole(
            @PathVariable("userId") UUID userId,
            @RequestBody List<Map<String, Object>> roles
    );

    @PutMapping("/admin/realms/${keycloak.realm}/users/{keycloakUserId}")
    ResponseEntity<Void> updateUser(
            @PathVariable String keycloakUserId,
            @RequestBody KeycloakUserRepresentation user
    );

    @GetMapping("/admin/realms/${keycloak.realm}/users/{keycloakUserId}")
    KeycloakUserRepresentation getUser(@PathVariable("keycloakUserId") String userId);
}
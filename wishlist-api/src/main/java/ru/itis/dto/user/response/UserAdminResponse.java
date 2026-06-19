package ru.itis.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminResponse {

    private UUID id;

    private String username;

    private String name;

    private String surname;

    private String email;

    private String phoneNumber;

    private String avatarUrl;

    private Boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant deletedAt;
}

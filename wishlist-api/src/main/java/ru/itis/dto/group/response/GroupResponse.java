package ru.itis.dto.group.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class GroupResponse {
    private UUID id;
    private String name;
    private String description;
    private Instant createdAt;
}

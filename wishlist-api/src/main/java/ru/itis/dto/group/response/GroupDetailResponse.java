package ru.itis.dto.group.response;

import lombok.Builder;
import lombok.Getter;
import ru.itis.dto.compilation.response.CompilationResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class GroupDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private Instant createdAt;
    private List<CompilationResponse> compilations;
    // null — не состоит в группе, иначе "member" / "admin" / "creator"
    private String memberStatus;
}

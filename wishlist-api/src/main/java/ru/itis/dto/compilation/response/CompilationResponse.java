package ru.itis.dto.compilation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.itis.dto.gift.GiftResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CompilationResponse {
    private UUID id;
    private String title;
    private String description;
    private int categoryId;
    private UUID groupId;
    private Instant createdAt;
    private List<GiftResponse> gifts;
}

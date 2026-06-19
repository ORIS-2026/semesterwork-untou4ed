package ru.itis.dto.gift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftResponse {
    private UUID id;
    private String title;
    private String description;
    private String link;
    private BigDecimal price;
    private UUID ownerId;
    private String ownerType;
    private Instant createdAt;
}

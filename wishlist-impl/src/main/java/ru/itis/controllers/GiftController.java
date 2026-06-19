package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.api.GiftApi;
import ru.itis.dto.gift.AddGiftRequest;
import ru.itis.dto.gift.GiftResponse;
import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;
import ru.itis.services.giftService.GiftService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class GiftController implements GiftApi {

    private final GiftService giftService;

    @Override
    public void addGift(Jwt jwt, AddGiftRequest request) {
        giftService.addGift(
                request.getName(),
                request.getDescription(),
                request.getLink(),
                BigDecimal.valueOf(request.getPrice()),
                UUID.fromString(request.getOwnerId()),
                GiftOwnerType.valueOf(request.getOwnerType().toUpperCase())
        );
    }

    @Override
    public List<GiftResponse> getGifts(UUID ownerId, String ownerType) {
        GiftOwnerType type = GiftOwnerType.valueOf(ownerType.toUpperCase());
        List<Gift> gifts = giftService.findGiftsByOwnerTypeAndOwnerId(type, ownerId);
        return toResponseList(gifts);
    }

    @Override
    public List<GiftResponse> searchGifts(String title, BigDecimal minPrice, BigDecimal maxPrice, String ownerType) {
        GiftOwnerType type = ownerType != null ? GiftOwnerType.valueOf(ownerType.toUpperCase()) : null;
        List<Gift> gifts = giftService.searchGifts(title, minPrice, maxPrice, type);
        return toResponseList(gifts);
    }

    private List<GiftResponse> toResponseList(List<Gift> gifts) {
        return gifts.stream()
                .map(g -> GiftResponse.builder()
                        .id(g.getId())
                        .title(g.getTitle())
                        .description(g.getDescription())
                        .link(g.getLink())
                        .price(g.getPrice())
                        .ownerId(g.getOwnerId())
                        .ownerType(g.getOwnerType().getValue())
                        .createdAt(g.getCreatedAt())
                        .build())
                .toList();
    }
}

package ru.itis.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import ru.itis.dto.gift.AddGiftRequest;
import ru.itis.dto.gift.GiftResponse;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/gifts")
@RestController
public interface GiftApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void addGift(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AddGiftRequest request
    );

    @GetMapping
    List<GiftResponse> getGifts(
            @RequestParam UUID ownerId,
            @RequestParam String ownerType
    );

    @GetMapping("/search")
    List<GiftResponse> searchGifts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String ownerType
    );
}

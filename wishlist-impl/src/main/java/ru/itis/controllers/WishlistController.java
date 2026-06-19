package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.api.WishlistApi;
import ru.itis.dto.wishlist.request.UpdateWishlistRequest;
import ru.itis.dto.wishlist.response.WishlistResponse;
import ru.itis.services.wishlistService.WishlistService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WishlistController implements WishlistApi {

    private final WishlistService wishlistService;

    @Override
    public Slice<WishlistResponse> getSubscribedWishlists(Jwt jwt, int page, int size) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return wishlistService.findSubscribedWishlists(userId, page, size);
    }

    @Override
    public WishlistResponse getUserWishlist(UUID userId) {
        return wishlistService.getUserWishlist(userId);
    }

    @Override
    public WishlistResponse updateMyWishlist(Jwt jwt, UpdateWishlistRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return wishlistService.updateMyWishlist(userId, request);
    }
}

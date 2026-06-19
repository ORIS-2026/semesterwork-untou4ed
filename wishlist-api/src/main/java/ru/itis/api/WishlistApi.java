package ru.itis.api;

import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.dto.wishlist.request.UpdateWishlistRequest;
import ru.itis.dto.wishlist.response.WishlistResponse;

import java.util.UUID;

@RequestMapping("/api/v1/wishlists")
@RestController
public interface WishlistApi {

    @GetMapping("/feed")
    Slice<WishlistResponse> getSubscribedWishlists(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @GetMapping("/user/{userId}")
    WishlistResponse getUserWishlist(@PathVariable UUID userId);

    @PatchMapping("/me")
    WishlistResponse updateMyWishlist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateWishlistRequest request
    );
}

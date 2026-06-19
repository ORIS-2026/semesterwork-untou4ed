package ru.itis.services.wishlistService;

import org.springframework.data.domain.Slice;
import ru.itis.dto.wishlist.request.UpdateWishlistRequest;
import ru.itis.dto.wishlist.response.WishlistResponse;
import ru.itis.entities.User;

import java.util.UUID;

public interface WishlistService {

    Slice<WishlistResponse> findSubscribedWishlists(UUID userId, int page, int size);

    WishlistResponse getUserWishlist(UUID authorId);

    WishlistResponse updateMyWishlist(UUID userId, UpdateWishlistRequest request);

    void createUserWishlist(User user);
}

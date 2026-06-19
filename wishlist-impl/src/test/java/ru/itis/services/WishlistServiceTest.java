package ru.itis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import ru.itis.dto.wishlist.request.UpdateWishlistRequest;
import ru.itis.dto.wishlist.response.WishlistResponse;
import ru.itis.entities.User;
import ru.itis.entities.Wishlist;
import ru.itis.exceptions.WishlistNotFoundException;
import ru.itis.mappers.WishlistMapper;
import ru.itis.repositories.WishlistRepository;
import ru.itis.services.wishlistService.WishlistServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock WishlistRepository wishlistRepository;
    @Mock WishlistMapper wishlistMapper;
    @InjectMocks WishlistServiceImpl wishlistService;

    private User user() {
        return User.builder().id(UUID.randomUUID()).username("user1").name("Ivan").enabled(true).phoneNumber("+79001234567").build();
    }

    private Wishlist wishlist(User author) {
        return Wishlist.builder().id(UUID.randomUUID()).title("Мой вишлист").author(author).build();
    }

    private WishlistResponse response(Wishlist w) {
        return WishlistResponse.builder().id(w.getId()).title(w.getTitle())
                .authorId(w.getAuthor().getId()).authorUsername(w.getAuthor().getUsername()).build();
    }

    @Test
    void getUserWishlist_found() {
        User user = user();
        Wishlist wishlist = wishlist(user);
        WishlistResponse expected = response(wishlist);

        when(wishlistRepository.findByAuthorId(user.getId())).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toResponse(wishlist)).thenReturn(expected);

        WishlistResponse result = wishlistService.getUserWishlist(user.getId());

        assertThat(result.getId()).isEqualTo(wishlist.getId());
        assertThat(result.getAuthorUsername()).isEqualTo("user1");
    }

    @Test
    void getUserWishlist_notFound_throws() {
        UUID userId = UUID.randomUUID();
        when(wishlistRepository.findByAuthorId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.getUserWishlist(userId))
                .isInstanceOf(WishlistNotFoundException.class);
    }

    @Test
    void updateMyWishlist_updatesFields() {
        User user = user();
        Wishlist wishlist = wishlist(user);
        UpdateWishlistRequest req = new UpdateWishlistRequest("Новое название", "Описание");
        WishlistResponse expected = WishlistResponse.builder().id(wishlist.getId()).title("Новое название").build();

        when(wishlistRepository.findByAuthorId(user.getId())).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toResponse(any())).thenReturn(expected);

        WishlistResponse result = wishlistService.updateMyWishlist(user.getId(), req);

        assertThat(wishlist.getTitle()).isEqualTo("Новое название");
        assertThat(result.getTitle()).isEqualTo("Новое название");
    }

    @Test
    void updateMyWishlist_notFound_throws() {
        UUID userId = UUID.randomUUID();
        when(wishlistRepository.findByAuthorId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.updateMyWishlist(userId, new UpdateWishlistRequest("x", null)))
                .isInstanceOf(WishlistNotFoundException.class);
    }

    @Test
    void findSubscribedWishlists_returnsMapped() {
        UUID userId = UUID.randomUUID();
        User author = user();
        Wishlist w = wishlist(author);
        WishlistResponse mapped = response(w);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(wishlistRepository.findSubscribedWishlistsByUserId(eq(userId), any()))
                .thenReturn(new SliceImpl<>(List.of(w)));
        when(wishlistMapper.toResponse(w)).thenReturn(mapped);

        var result = wishlistService.findSubscribedWishlists(userId, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthorUsername()).isEqualTo("user1");
    }

    @Test
    void createUserWishlist_savesWishlist() {
        User user = user();

        wishlistService.createUserWishlist(user);

        verify(wishlistRepository).save(argThat(w -> w.getAuthor().equals(user)));
    }
}

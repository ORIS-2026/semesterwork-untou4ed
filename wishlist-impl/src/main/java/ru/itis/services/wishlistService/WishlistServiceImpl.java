package ru.itis.services.wishlistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.dto.gift.GiftResponse;
import ru.itis.dto.wishlist.request.UpdateWishlistRequest;
import ru.itis.dto.wishlist.response.WishlistResponse;
import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;
import ru.itis.entities.User;
import ru.itis.entities.Wishlist;
import ru.itis.exceptions.WishlistNotFoundException;
import ru.itis.mappers.GiftMapper;
import ru.itis.mappers.WishlistMapper;
import ru.itis.repositories.GiftRepository;
import ru.itis.repositories.WishlistRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMapper     wishlistMapper;
    private final GiftRepository     giftRepository;
    private final GiftMapper         giftMapper;

    @Override
    public Slice<WishlistResponse> findSubscribedWishlists(UUID userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Wishlist> slice = wishlistRepository.findSubscribedWishlistsByUserId(userId, pageable);

        // Один batch-запрос для всех вишлистов страницы — решает N+1
        Map<UUID, List<GiftResponse>> giftsMap = loadGiftsMap(
                slice.getContent().stream().map(Wishlist::getId).toList()
        );

        return slice.map(w -> wishlistMapper.toResponse(w).toBuilder()
                .gifts(giftsMap.getOrDefault(w.getId(), List.of()))
                .build());
    }

    @Override
    public WishlistResponse getUserWishlist(UUID authorId) {
        Wishlist wishlist = wishlistRepository.findByAuthorId(authorId)
                .orElseThrow(() -> new WishlistNotFoundException("Вишлист не найден"));

        List<GiftResponse> gifts = giftRepository
                .findByOwnerIdAndOwnerType(wishlist.getId(), GiftOwnerType.WISHLIST)
                .stream()
                .map(giftMapper::toResponse)
                .toList();

        return wishlistMapper.toResponse(wishlist).toBuilder()
                .gifts(gifts)
                .build();
    }

    @Override
    @Transactional
    public WishlistResponse updateMyWishlist(UUID userId, UpdateWishlistRequest request) {
        Wishlist wishlist = wishlistRepository.findByAuthorId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Вишлист не найден"));

        if (request.getTitle() != null) {
            wishlist.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            wishlist.setDescription(request.getDescription());
        }

        log.info("Обновлён вишлист пользователя {}", userId);
        return wishlistMapper.toResponse(wishlist).toBuilder()
                .gifts(List.of())
                .build();
    }

    @Override
    public void createUserWishlist(User author) {
        Wishlist wishlist = Wishlist.builder()
                .author(author)
                .build();
        wishlistRepository.save(wishlist);
    }

    // SELECT * FROM gifts WHERE owner_id IN (...) AND owner_type = 'WISHLIST'
    private Map<UUID, List<GiftResponse>> loadGiftsMap(List<UUID> wishlistIds) {
        if (wishlistIds.isEmpty()) return Map.of();
        return giftRepository
                .findByOwnerIdInAndOwnerType(wishlistIds, GiftOwnerType.WISHLIST)
                .stream()
                .collect(Collectors.groupingBy(
                        Gift::getOwnerId,
                        Collectors.mapping(giftMapper::toResponse, Collectors.toList())
                ));
    }
}

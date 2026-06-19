package ru.itis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;
import ru.itis.repositories.GiftRepository;
import ru.itis.services.giftService.GiftServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftServiceTest {

    @Mock GiftRepository giftRepository;
    @InjectMocks GiftServiceImpl giftService;

    private Gift gift(UUID ownerId, GiftOwnerType ownerType) {
        return Gift.builder()
                .id(UUID.randomUUID())
                .title("Подарок")
                .description("Описание")
                .link("https://example.com")
                .price(new BigDecimal("999.99"))
                .ownerId(ownerId)
                .ownerType(ownerType)
                .build();
    }

    @Test
    void addGift_savesAndReturns() {
        UUID ownerId = UUID.randomUUID();
        Gift saved = gift(ownerId, GiftOwnerType.WISHLIST);

        when(giftRepository.save(any(Gift.class))).thenReturn(saved);

        Gift result = giftService.addGift("Подарок", "Описание", "https://example.com",
                new BigDecimal("999.99"), ownerId, GiftOwnerType.WISHLIST);

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getTitle()).isEqualTo("Подарок");
        assertThat(result.getOwnerType()).isEqualTo(GiftOwnerType.WISHLIST);
        verify(giftRepository).save(any(Gift.class));
    }

    @Test
    void addGift_toCompilation() {
        UUID compilationId = UUID.randomUUID();
        Gift saved = gift(compilationId, GiftOwnerType.COMPILATION);

        when(giftRepository.save(any(Gift.class))).thenReturn(saved);

        Gift result = giftService.addGift("Книга", null, null,
                new BigDecimal("500.00"), compilationId, GiftOwnerType.COMPILATION);

        assertThat(result.getOwnerType()).isEqualTo(GiftOwnerType.COMPILATION);
    }

    @Test
    void findGiftsByOwnerTypeAndOwnerId_returnsList() {
        UUID ownerId = UUID.randomUUID();
        List<Gift> gifts = List.of(gift(ownerId, GiftOwnerType.WISHLIST), gift(ownerId, GiftOwnerType.WISHLIST));

        when(giftRepository.findByOwnerIdAndOwnerType(ownerId, GiftOwnerType.WISHLIST)).thenReturn(gifts);

        List<Gift> result = giftService.findGiftsByOwnerTypeAndOwnerId(GiftOwnerType.WISHLIST, ownerId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(g -> g.getOwnerType() == GiftOwnerType.WISHLIST);
    }

    @Test
    void findGiftsByOwnerTypeAndOwnerId_empty() {
        UUID ownerId = UUID.randomUUID();
        when(giftRepository.findByOwnerIdAndOwnerType(ownerId, GiftOwnerType.COMPILATION)).thenReturn(List.of());

        List<Gift> result = giftService.findGiftsByOwnerTypeAndOwnerId(GiftOwnerType.COMPILATION, ownerId);

        assertThat(result).isEmpty();
    }
}

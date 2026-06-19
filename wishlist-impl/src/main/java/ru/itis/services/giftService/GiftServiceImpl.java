package ru.itis.services.giftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;
import ru.itis.repositories.GiftRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftServiceImpl implements GiftService {

    private final GiftRepository giftRepository;

    @Override
    public Gift addGift(String title, String description, String link, BigDecimal price,
                        UUID ownerId, GiftOwnerType ownerType) {
        Gift gift = Gift.builder()
                .title(title)
                .description(description)
                .link(link)
                .price(price)
                .ownerId(ownerId)
                .ownerType(ownerType)
                .build();

        Gift saved = giftRepository.save(gift);
        log.info("Добавлен подарок id={}, ownerId={}, ownerType={}", saved.getId(), ownerId, ownerType);
        return saved;
    }

    @Override
    public List<Gift> findGiftsByOwnerTypeAndOwnerId(GiftOwnerType ownerType, UUID ownerId) {
        return giftRepository.findByOwnerIdAndOwnerType(ownerId, ownerType);
    }

    @Override
    public List<Gift> searchGifts(String title, BigDecimal minPrice, BigDecimal maxPrice, GiftOwnerType ownerType) {
        return giftRepository.searchGifts(title, minPrice, maxPrice, ownerType);
    }
}

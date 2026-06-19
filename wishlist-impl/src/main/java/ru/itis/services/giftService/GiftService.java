package ru.itis.services.giftService;

import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GiftService {

    Gift addGift(String title, String description, String link, BigDecimal price,
                 UUID ownerId, GiftOwnerType ownerType);

    List<Gift> findGiftsByOwnerTypeAndOwnerId(GiftOwnerType ownerType, UUID ownerId);

    List<Gift> searchGifts(String title, BigDecimal minPrice, BigDecimal maxPrice, GiftOwnerType ownerType);
}

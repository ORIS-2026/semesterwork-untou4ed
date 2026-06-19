package ru.itis.repositories;

import ru.itis.entities.Gift;
import ru.itis.entities.GiftOwnerType;

import java.math.BigDecimal;
import java.util.List;

public interface GiftRepositoryCustom {

    List<Gift> searchGifts(String title, BigDecimal minPrice, BigDecimal maxPrice, GiftOwnerType ownerType);
}

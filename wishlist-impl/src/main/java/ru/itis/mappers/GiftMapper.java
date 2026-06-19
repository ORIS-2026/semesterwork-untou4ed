package ru.itis.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itis.dto.gift.GiftResponse;
import ru.itis.entities.Gift;

@Mapper(componentModel = "spring")
public interface GiftMapper {

    @Mapping(target = "ownerType", expression = "java(gift.getOwnerType().getValue())")
    GiftResponse toResponse(Gift gift);
}

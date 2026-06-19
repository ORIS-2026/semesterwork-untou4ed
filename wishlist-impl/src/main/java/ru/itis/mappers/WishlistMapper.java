package ru.itis.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itis.dto.wishlist.response.WishlistResponse;
import ru.itis.entities.Wishlist;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "gifts", ignore = true)
    WishlistResponse toResponse(Wishlist wishlist);
}

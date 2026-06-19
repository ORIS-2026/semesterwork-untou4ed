package ru.itis.dto.wishlist.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWishlistRequest {
    private String title;
    private String description;
}

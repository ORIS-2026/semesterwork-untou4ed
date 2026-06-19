package ru.itis.dto.gift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddGiftRequest {
    private String name;
    private String description;
    private String link;
    private float price;
    private String ownerType;
    private String ownerId;
}

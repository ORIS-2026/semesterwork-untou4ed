package ru.itis.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.itis.entities.GiftOwnerType;

import java.util.Arrays;

@Converter(autoApply = true)
public class GiftOwnerTypeConverter implements AttributeConverter<GiftOwnerType, String> {

    @Override // перед repository.save(gift)
    public String convertToDatabaseColumn(GiftOwnerType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    // после чтения из бд
    @Override
    public GiftOwnerType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Arrays.stream(GiftOwnerType.values())
                .filter(e -> e.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный owner_type: " + dbData));
    }
}

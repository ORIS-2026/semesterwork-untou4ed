package ru.itis.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.itis.entities.GroupMemberStatus;

import java.util.Arrays;

@Converter(autoApply = true)
public class GroupMemberStatusConverter implements AttributeConverter<GroupMemberStatus, String> {

    @Override
    public String convertToDatabaseColumn(GroupMemberStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public GroupMemberStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Arrays.stream(GroupMemberStatus.values())
                .filter(e -> e.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус участника: " + dbData));
    }
}

package ru.itis.mappers;

import org.mapstruct.Mapper;
import ru.itis.dto.group.response.GroupResponse;
import ru.itis.entities.Group;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupResponse toResponse(Group group);
}

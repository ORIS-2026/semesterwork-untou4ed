package ru.itis.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itis.dto.compilation.response.CompilationResponse;
import ru.itis.entities.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "gifts", ignore = true)
    CompilationResponse toResponse(Compilation compilation);
}

package ru.itis.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.itis.dto.user.response.UserAdminResponse;
import ru.itis.dto.user.response.UserMeResponse;
import ru.itis.dto.user.response.UserResponse;
import ru.itis.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserMeResponse toUserMeResponse(User user);

    UserResponse toUserResponse(User user);

    UserAdminResponse toUserAdminResponse(User user);
}

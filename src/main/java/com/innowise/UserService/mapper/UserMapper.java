package com.innowise.UserService.mapper;

import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "appUser", ignore = true)
    UserDto toDto(User user);

    User toEntity(UserDto dto);

}

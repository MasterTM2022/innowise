package com.innowise.UserService.mapper;

import com.innowise.UserService.dto.UserDto;
import com.innowise.UserService.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);

}

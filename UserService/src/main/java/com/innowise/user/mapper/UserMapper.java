package com.innowise.user.mapper;

import com.innowise.common.dto.UserDto;
import com.innowise.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);

}

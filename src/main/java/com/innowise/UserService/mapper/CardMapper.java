package com.innowise.UserService.mapper;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.entity.Card;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target="userEmail", source = "user.email")
    CardDto toDto(Card card);

    @InheritInverseConfiguration
    Card toEntity(CardDto dto);
}
package com.innowise.UserService.mapper;

import com.innowise.UserService.dto.CardDto;
import com.innowise.UserService.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardDto toDto(Card card);

    Card toEntity(CardDto dto);
}
package com.innowise.user.mapper;

import com.innowise.user.dto.CardDto;
import com.innowise.user.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "userId", source = "user.id")
    CardDto toDto(Card card);

    default Card toEntity(CardDto dto) {
        Card card = new Card();
        card.setNumber(dto.getNumber());
        card.setHolder(dto.getHolder());
        card.setExpirationDate(dto.getExpirationDate());
        return card;
    }
}
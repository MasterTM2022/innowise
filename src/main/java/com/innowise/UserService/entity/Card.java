package com.innowise.UserService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "card_info",
        indexes = {
                @Index(name = "idx_card_info_user_id", columnList = "user_id"),
                @Index(name = "idx_card_info_expiration_date", columnList = "expiration_date"),
                @Index(name = "idx_card_info_user_id_expiration_date", columnList = "user_id, expiration_date")
        }
)
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true)
    private String number;

    @Column(nullable = false)
    private String holder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;
}
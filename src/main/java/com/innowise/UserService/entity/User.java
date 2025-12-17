package com.innowise.UserService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_surname_name", columnList = "surname, name"),
                @Index(name = "idx_users_full_search", columnList = "surname, name, birth_date")
        }
)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(unique = true, nullable = false)
    private String email;

    @OneToOne
    @JoinColumn(name = "app_user_id", nullable = false, unique = true)
    private AppUser appUser;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    //    // Getters and Setters
    public void addCard(Card card) {
        cards.add(card);
        card.setUser(this);
    }

    public void removeCard(Card card) {
        cards.remove(card);
        card.setUser(null);
    }
}
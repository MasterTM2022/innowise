package com.innowise.UserService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "items",
        indexes = {
                @Index(name = "idx_items_name", columnList = "name"),
                @Index(name = "idx_items_price", columnList = "price")
        })
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
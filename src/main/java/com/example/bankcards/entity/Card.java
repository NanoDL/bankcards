package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Data
public class Card {

    @Id
    @GeneratedValue
    private Long id;
    @NotBlank
    private String pan;
    private LocalDate expirationDate;

    private Integer balance;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;






}

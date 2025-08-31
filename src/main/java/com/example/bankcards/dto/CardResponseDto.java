package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardResponseDto {

    private Long id;
    private String pan;
    private LocalDate expirationDate;
    private String ownerName;
    private Integer balance;
    private CardStatus status;
}

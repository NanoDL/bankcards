package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CardCreateDto {
    @NotBlank
    private Integer owner;
}

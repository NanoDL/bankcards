package com.example.bankcards.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationDto {
    @NotBlank
    @Min(5)
    private String username;
    @NotBlank
    @Min(8)
    private String password;
}

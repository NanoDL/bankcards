package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterDto {
    @NotBlank
    @Min(5)
    private String username;
    @NotBlank
    @Min(8)
    private String password;
    @Email
    @NotBlank
    private String email;
}

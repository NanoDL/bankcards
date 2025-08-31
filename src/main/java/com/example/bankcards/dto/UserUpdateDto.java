package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {
    @NotBlank
    @Min(5)
    private String username;
    @NotBlank
    @Min(8)
    private String password;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private UserRole role;
    private boolean enabled;
}

package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue
    private Long id;
    @Min(5)
    @NotBlank

    private String username;
    @Min(8)
    @NotBlank
    private String password;
    @Email
    @NotBlank
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;


    private boolean enabled = true;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards;

}

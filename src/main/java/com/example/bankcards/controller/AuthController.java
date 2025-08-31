package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthenticationDto;
import com.example.bankcards.dto.UserRegisterDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.RegistrationService;
import com.example.bankcards.service.UserRegistrationService;
import com.example.bankcards.util.JWTUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserRegistrationService userRegistrationService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UserRegistrationService userRegistrationService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRegistrationService = userRegistrationService;
    }
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AuthenticationDto authenticationDto){
        System.out.println(authenticationDto.getUsername() + authenticationDto.getPassword());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authenticationDto.getUsername(), authenticationDto.getPassword());
        try{
            authenticationManager.authenticate(authenticationToken);
            System.out.println("TRY");
        } catch (AuthenticationException e){
            throw new BadCredentialsException("Bad credentials");
        }
        System.out.println("AUUUUU");

        String token = jwtUtil.generateToken(authenticationDto.getUsername());
        System.out.println(token);
        return  Map.of("jwt-token", token);

    }

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody UserRegisterDto dto) {
        userRegistrationService.registerCustomer(dto);
        String token = jwtUtil.generateToken(dto.getUsername());
        return Map.of("jwt-token", token);
    }
}

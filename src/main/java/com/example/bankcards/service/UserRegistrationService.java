package com.example.bankcards.service;

import com.example.bankcards.dto.UserRegisterDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserRegistrationService {


    private final UserRepository myUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationService(UserRepository myUserRepository, PasswordEncoder passwordEncoder) {
        this.myUserRepository = myUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerCustomer(UserRegisterDto dto) {
        // Проверяем, не существует ли уже пользователь с таким username или email
        if (myUserRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Пользователь с таким именем уже существует");
        }
        if (myUserRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Пользователь с таким email уже существует");

        }

        // Создаем пользователя
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.USER);

        // Сохраняем пользователя
        user = myUserRepository.save(user);


        // Сохраняем клиента
        return user;
    }
}

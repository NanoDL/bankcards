package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.BankUserDetails;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;
    private ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public User getUserFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||"anonymousUser".equals(authentication.getPrincipal())) {
            throw new ForbiddenException("You are not logged in");
        } else {

            BankUserDetails userDetails = (BankUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }

    }

    public Page<UserResponseDto> getUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return page.map(c -> modelMapper.map(c, UserResponseDto.class));
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        modelMapper.map(userUpdateDto, user);
        userRepository.save(user);
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        User user = new User();
        modelMapper.map(userCreateDto, user);
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Transactional
    public UserResponseDto blockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
        return modelMapper.map(user, UserResponseDto.class);
    }

}

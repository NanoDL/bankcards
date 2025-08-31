package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //admin
    @GetMapping
    public Page<UserResponseDto> getUsers(@RequestParam(required = false, defaultValue = "0") Integer page,
                                          @RequestParam(required = false, defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.getUsers(pageable);
    }

    //admin
    @PutMapping({"/{id}"})
    public UserResponseDto updateUser(@PathVariable Long id,
                             @RequestBody UserUpdateDto userUpdateDto) {
        return userService.updateUser(id, userUpdateDto);
    }
    //admin
    @PostMapping
    public UserResponseDto createUser(@RequestBody UserCreateDto userCreateDto) {
        return userService.createUser(userCreateDto);
    }
    //admin
    @DeleteMapping({"/{id}"})
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    //admin
    @PatchMapping({"/{id}/block"})
    public UserResponseDto blockUser(@PathVariable Long id) {
        return userService.blockUser(id);
    }
}

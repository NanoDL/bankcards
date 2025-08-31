package com.example.bankcards.exception;

import lombok.AllArgsConstructor;
import lombok.Data;



@AllArgsConstructor
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}

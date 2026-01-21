package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class CardNotFoundException extends RestException {
    public CardNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND); // 404
    }
}
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends RestException {
    public InsufficientFundsException(String message) {
        super(message, HttpStatus.BAD_REQUEST); // 400
    }
}
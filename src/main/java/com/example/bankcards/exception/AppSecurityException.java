package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class AppSecurityException extends RestException {
    public AppSecurityException(String message) {
        super(message, HttpStatus.FORBIDDEN); // 403
    }
}
package com.company.vacation.exception;

public class ApiValidationException extends RuntimeException {

    public ApiValidationException(String message) {
        super(message);
    }
}

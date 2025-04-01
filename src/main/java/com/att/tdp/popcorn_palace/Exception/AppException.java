package com.att.tdp.popcorn_palace.Exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorType errorType;
    private final String details;

    public AppException(String message, HttpStatus status, ErrorType errorType) {
        this(message, status, errorType, null);
    }

    public AppException(String message, HttpStatus status, ErrorType errorType, String details) {
        super(message);
        this.status = status;
        this.errorType = errorType;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getDetails() {
        return details;
    }
}

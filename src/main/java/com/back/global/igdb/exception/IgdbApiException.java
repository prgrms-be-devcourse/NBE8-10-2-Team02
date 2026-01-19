package com.back.global.igdb.exception;

public class IgdbApiException extends RuntimeException {
    public IgdbApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
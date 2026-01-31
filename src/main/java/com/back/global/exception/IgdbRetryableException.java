package com.back.global.exception;

public class IgdbRetryableException extends RuntimeException {

    public IgdbRetryableException(String actionName, int statusCode, Throwable cause) {
        super("%s 실패, status: %d".formatted(actionName, statusCode), cause);
    }
}

package com.back.global.steam.exception;

public class SteamRetryableException extends RuntimeException {

    public SteamRetryableException(String actionName, int statusCode, Throwable cause) {
        super("%s 실패, status: %d".formatted(actionName, statusCode), cause);
    }
}

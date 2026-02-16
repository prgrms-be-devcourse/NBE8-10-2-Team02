package com.back.global.steam.exception;

public class SteamApiException extends RuntimeException {

    public SteamApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

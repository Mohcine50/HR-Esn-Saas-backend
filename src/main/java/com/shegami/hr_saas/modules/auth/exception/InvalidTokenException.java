package com.shegami.hr_saas.modules.auth.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {}
    public InvalidTokenException(String message, Throwable cause) {}
    public InvalidTokenException(Throwable cause) {}
}

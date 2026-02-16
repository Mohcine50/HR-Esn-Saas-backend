package com.shegami.hr_saas.modules.auth.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {}
    public TokenExpiredException(String message) {}
    public TokenExpiredException(String message, Throwable cause) {}
}

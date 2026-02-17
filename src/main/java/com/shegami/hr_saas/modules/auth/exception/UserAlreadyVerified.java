package com.shegami.hr_saas.modules.auth.exception;

public class UserAlreadyVerified extends RuntimeException {
    public UserAlreadyVerified() {}
    public UserAlreadyVerified(String message) {
        super(message);
    }
    public UserAlreadyVerified(String message, Throwable cause) {
        super(message, cause);
    }
    public UserAlreadyVerified(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

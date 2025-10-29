package com.shegami.hr_saas.modules.auth.exception;

public class UserRoleNotFoundException extends RuntimeException {

    public UserRoleNotFoundException(Throwable cause) {
        super(cause);
    }

    public UserRoleNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UserRoleNotFoundException() {
    }
}

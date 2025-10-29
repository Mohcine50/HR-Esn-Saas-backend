package com.shegami.hr_saas.modules.auth.exception;

public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException() {
    }

    public TenantNotFoundException(String message) {
        super(message);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TenantNotFoundException(Throwable cause) {
        super(cause);
    }

    public TenantNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

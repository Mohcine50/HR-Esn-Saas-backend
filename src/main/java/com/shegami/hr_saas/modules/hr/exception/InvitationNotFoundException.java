package com.shegami.hr_saas.modules.hr.exception;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException(String message) {
        super(message);
    }
    public InvitationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

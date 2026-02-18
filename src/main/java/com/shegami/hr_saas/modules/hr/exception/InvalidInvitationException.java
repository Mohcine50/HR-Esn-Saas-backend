package com.shegami.hr_saas.modules.hr.exception;

public class InvalidInvitationException extends RuntimeException {
    public InvalidInvitationException() {
        super();
    }
    public InvalidInvitationException(String message) {
        super(message);
    }
    public InvalidInvitationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.shegami.hr_saas.modules.hr.exception;

public class InvitationAlreadyAccepted extends RuntimeException {
    public InvitationAlreadyAccepted(String message) {
        super(message);
    }
    public InvitationAlreadyAccepted(String message, Throwable cause) {
        super(message, cause);
    }
}

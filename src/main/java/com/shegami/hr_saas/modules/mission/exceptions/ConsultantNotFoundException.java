package com.shegami.hr_saas.modules.mission.exceptions;

public class ConsultantNotFoundException extends RuntimeException {
    public ConsultantNotFoundException() {
        super("Consultant not found");
    }
    public ConsultantNotFoundException(String message) {
        super(message);
    }
}

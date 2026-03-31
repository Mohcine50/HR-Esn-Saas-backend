package com.shegami.hr_saas.modules.timesheet.exceptions;

public class TimesheetModificationForbiddenException extends RuntimeException {
    public TimesheetModificationForbiddenException(String message) {
        super(message);
    }
}

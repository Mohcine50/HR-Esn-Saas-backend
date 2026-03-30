package com.shegami.hr_saas.modules.timesheet.exceptions;

public class EmptyTimesheetException extends RuntimeException {
    public EmptyTimesheetException(String message) {
        super(message);
    }
}

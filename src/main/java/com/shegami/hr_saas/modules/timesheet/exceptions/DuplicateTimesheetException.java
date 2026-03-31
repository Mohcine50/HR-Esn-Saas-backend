package com.shegami.hr_saas.modules.timesheet.exceptions;

public class DuplicateTimesheetException extends RuntimeException {
    public DuplicateTimesheetException(String message) {
        super(message);
    }
}

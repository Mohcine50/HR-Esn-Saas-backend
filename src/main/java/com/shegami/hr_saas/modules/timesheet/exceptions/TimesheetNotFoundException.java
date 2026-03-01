package com.shegami.hr_saas.modules.timesheet.exceptions;

public class TimesheetNotFoundException extends RuntimeException {
    public TimesheetNotFoundException(String message) {
        super(message);
    }
}

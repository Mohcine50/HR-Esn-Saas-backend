package com.shegami.hr_saas.modules.timesheet.exceptions;

public class IllegalTimesheetStateException extends RuntimeException {
    public IllegalTimesheetStateException(String message) {
        super(message);
    }
}

package com.shegami.hr_saas.modules.billing.exception;

public class InvoiceFileNotFoundException extends RuntimeException {
    public InvoiceFileNotFoundException(String message) {
        super(message);
    }
}

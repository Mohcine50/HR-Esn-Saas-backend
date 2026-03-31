package com.shegami.hr_saas.modules.billing.exception;

public class IllegalInvoiceStateException extends RuntimeException {
    public IllegalInvoiceStateException(String message) {
        super(message);
    }
}

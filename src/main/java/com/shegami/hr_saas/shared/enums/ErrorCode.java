package com.shegami.hr_saas.shared.enums;

import lombok.Getter;

public enum ErrorCode {

    // ── Auth ──────────────────────────────────────────────────────────────────
    INVALID_TOKEN("Invalid or malformed token"),
    TOKEN_EXPIRED("Token has expired"),
    TOKEN_NOT_FOUND("Token not found"),
    INVALID_INVITATION("Invitation is invalid"),
    INVITATION_EXPIRED("Invitation has expired"),
    INVITATION_ALREADY_USED("Invitation has already been accepted"),
    INVITATION_NOT_FOUND("Invitation not found"),
    PASSWORD_MISMATCH("Passwords do not match"),
    USER_ALREADY_VERIFIED("User account is already verified"),

    // ── User ──────────────────────────────────────────────────────────────────
    USER_NOT_FOUND("User not found"),
    USER_ALREADY_EXISTS("User already exists"),
    USER_ROLE_NOT_FOUND("User role not found"),

    // ── Tenant ────────────────────────────────────────────────────────────────
    TENANT_NOT_FOUND("Tenant not found"),

    // ── Employee ──────────────────────────────────────────────────────────────
    EMPLOYEE_NOT_FOUND("Employee not found"),
    EMPLOYEE_ALREADY_EXISTS("Employee already exists"),

    // ── Mission ───────────────────────────────────────────────────────────────
    MISSION_NOT_FOUND("Mission not found"),
    PROJECT_NOT_FOUND("Project not found"),
    CONSULTANT_NOT_FOUND("Consultant not found"),
    CLIENT_NOT_FOUND("Client not found"),

    // ── Timesheet ─────────────────────────────────────────────────────────────
    TIMESHEET_NOT_FOUND("Timesheet not found"),
    EMPTY_TIMESHEET("Cannot submit an empty timesheet"),
    TIMESHEET_MODIFICATION_FORBIDDEN("Timesheet is locked and cannot be modified"),
    DUPLICATE_TIMESHEET("A timesheet already exists for this period"),
    ILLEGAL_TIMESHEET_STATE("Illegal state transition for timesheet"),

    // ── Billing ──────────────────────────────────────────────────────────────
    INVOICE_NOT_FOUND("Invoice not found"),
    ILLEGAL_INVOICE_STATE("Illegal state transition for invoice"),
    INVOICE_FILE_NOT_FOUND("Invoice PDF file not found"),

    // ── Storage ───────────────────────────────────────────────────────────────
    STORAGE_UPLOAD_FAILED("File upload failed"),

    // ── Generic ───────────────────────────────────────────────────────────────
    RESOURCE_NOT_FOUND("Requested resource not found"),
    VALIDATION_ERROR("Validation failed"),
    BAD_REQUEST("Bad request"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred");

    @Getter
    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getCode() {
        return this.name();
    }
}
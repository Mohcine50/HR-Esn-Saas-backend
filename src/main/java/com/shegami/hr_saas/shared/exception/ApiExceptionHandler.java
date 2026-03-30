package com.shegami.hr_saas.shared.exception;

import com.shegami.hr_saas.modules.auth.exception.*;
import com.shegami.hr_saas.modules.billing.exception.InvoiceNotFoundException;
import com.shegami.hr_saas.modules.hr.exception.*;
import com.shegami.hr_saas.modules.mission.exceptions.ConsultantNotFoundException;
import com.shegami.hr_saas.modules.mission.exceptions.MissionNotFoundException;
import com.shegami.hr_saas.modules.mission.exceptions.ProjectNotFoundException;
import com.shegami.hr_saas.modules.timesheet.exceptions.*;
import com.shegami.hr_saas.modules.upload.exceptions.StorageUploadException;
import com.shegami.hr_saas.shared.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiException> handleInvalidToken(InvalidTokenException ex) {
        return build(ErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiException> handleTokenExpired(TokenExpiredException ex) {
        return build(ErrorCode.TOKEN_EXPIRED, HttpStatus.GONE);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiException> handleTokenNotFound(TokenNotFoundException ex) {
        return build(ErrorCode.TOKEN_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidInvitationException.class)
    public ResponseEntity<ApiException> handleInvalidInvitation(InvalidInvitationException ex) {
        return build(ErrorCode.INVALID_INVITATION, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InvitationExpiredException.class)
    public ResponseEntity<ApiException> handleInvitationExpired(InvitationExpiredException ex) {
        return build(ErrorCode.INVITATION_EXPIRED, HttpStatus.GONE);
    }

    @ExceptionHandler(InvitationAlreadyAccepted.class)
    public ResponseEntity<ApiException> handleInvitationAlreadyAccepted(InvitationAlreadyAccepted ex) {
        return build(ErrorCode.INVITATION_ALREADY_USED, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<ApiException> handleInvitationNotFound(InvitationNotFoundException ex) {
        return build(ErrorCode.INVITATION_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiException> handlePasswordMismatch(PasswordMismatchException ex) {
        return build(ErrorCode.PASSWORD_MISMATCH, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyVerified.class)
    public ResponseEntity<ApiException> handleUserAlreadyVerified(UserAlreadyVerified ex) {
        return build(ErrorCode.USER_ALREADY_VERIFIED, HttpStatus.CONFLICT);
    }

    // ── User ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiException> handleUserNotFound(UserNotFoundException ex) {
        return build(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiException> handleUserAlreadyExists(UserAlreadyExistException ex) {
        return build(ErrorCode.USER_ALREADY_EXISTS, HttpStatus.CONFLICT); // ← was 404
    }

    @ExceptionHandler(UserRoleNotFoundException.class)
    public ResponseEntity<ApiException> handleUserRoleNotFound(UserRoleNotFoundException ex) {
        return build(ErrorCode.USER_ROLE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    // ── Tenant ────────────────────────────────────────────────────────────────

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiException> handleTenantNotFound(TenantNotFoundException ex) {
        return build(ErrorCode.TENANT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    // ── Employee ──────────────────────────────────────────────────────────────

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiException> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        return build(ErrorCode.EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmployeeAlreadyExistException.class)
    public ResponseEntity<ApiException> handleEmployeeAlreadyExists(EmployeeAlreadyExistException ex) {
        return build(ErrorCode.EMPLOYEE_ALREADY_EXISTS, HttpStatus.CONFLICT); // ← was 404
    }

    // ── Mission ───────────────────────────────────────────────────────────────

    @ExceptionHandler(MissionNotFoundException.class)
    public ResponseEntity<ApiException> handleMissionNotFound(MissionNotFoundException ex) {
        return build(ErrorCode.MISSION_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiException> handleProjectNotFound(ProjectNotFoundException ex) {
        return build(ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConsultantNotFoundException.class)
    public ResponseEntity<ApiException> handleConsultantNotFound(ConsultantNotFoundException ex) {
        return build(ErrorCode.CONSULTANT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    // ── Timesheet ─────────────────────────────────────────────────────────────

    @ExceptionHandler(TimesheetNotFoundException.class)
    public ResponseEntity<ApiException> handleTimesheetNotFound(TimesheetNotFoundException ex) {
        return build(ErrorCode.TIMESHEET_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyTimesheetException.class)
    public ResponseEntity<ApiException> handleEmptyTimesheet(EmptyTimesheetException ex) {
        return build(ErrorCode.EMPTY_TIMESHEET, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ── Billing ──────────────────────────────────────────────────────────────

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ApiException> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        return build(ErrorCode.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    @ExceptionHandler(StorageUploadException.class)
    public ResponseEntity<ApiException> handleStorageUpload(StorageUploadException ex) {
        return build(ErrorCode.STORAGE_UPLOAD_FAILED, HttpStatus.BAD_GATEWAY);
    }

    // ── Generic ───────────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiException> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiException> handleAlreadyExists(AlreadyExistsException ex) {
        return build(ErrorCode.BAD_REQUEST, HttpStatus.CONFLICT); // ← was 404
    }

    @ExceptionHandler(ApiRequestException.class)
    public ResponseEntity<ApiException> handleApiRequest(ApiRequestException ex) {
        return build(ex.getMessage(), ErrorCode.BAD_REQUEST.getCode(), HttpStatus.BAD_REQUEST);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()) // ← include field name
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", "VALIDATION_001");
        body.put("errors", errors);
        body.put("timestamp", new Date());

        return ResponseEntity.badRequest().body(body);
    }

    // ── Global fallback — catches anything not handled above ──────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiException> handleUnexpected(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex); // log full stack
        return build(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<ApiException> build(ErrorCode errorCode, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(new ApiException(
                        errorCode.getMessage(),
                        errorCode.getCode(),
                        status,
                        LocalDate.now()));
    }

    private ResponseEntity<ApiException> build(String message, String errorCode, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(new ApiException(message, errorCode, status,
                        LocalDate.now()));
    }
}
package com.shegami.hr_saas.shared.exception;

import com.shegami.hr_saas.modules.auth.exception.*;
import com.shegami.hr_saas.modules.hr.exception.EmployeeAlreadyExistException;
import com.shegami.hr_saas.modules.hr.exception.EmployeeNotFoundException;
import com.shegami.hr_saas.modules.hr.exception.InvitationExpiredException;
import com.shegami.hr_saas.modules.upload.exceptions.StorageUploadException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = ApiRequestException.class)
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException apiRequestException) {

        ApiException apiException = new ApiException(apiRequestException.getMessage(), HttpStatus.BAD_REQUEST, new Date());

        return new ResponseEntity<>(apiException, apiException.getHttpStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleRequestBodyValidation(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();


        return new ResponseEntity<>(getErrorsMap(errors), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(value = InvitationExpiredException.class)
    public ResponseEntity<Object> handleInvitationExpiredException(InvitationExpiredException invitationExpiredException) {

        ApiException apiException = new ApiException(invitationExpiredException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException resourceNotFoundException) {

        ApiException apiException = new ApiException(resourceNotFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = StorageUploadException.class)
    public ResponseEntity<Object> handleStorageUploadException(StorageUploadException storageUploadException) {

        ApiException apiException = new ApiException(storageUploadException.getMessage(), HttpStatus.FORBIDDEN, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException notFoundException) {

        ApiException apiException = new ApiException(notFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = AlreadyExistsException.class)
    public ResponseEntity<Object> handleResourceAlreadyExist(AlreadyExistsException alreadyExistsException) {

        ApiException apiException = new ApiException(alreadyExistsException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = UserAlreadyExistException.class)
    public ResponseEntity<Object> handleUserAlreadyExist(UserAlreadyExistException notFoundException) {

        ApiException apiException = new ApiException(notFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = TenantNotFoundException.class)
    public ResponseEntity<Object> handleTenantNotFoundException(TenantNotFoundException notFoundException) {

        ApiException apiException = new ApiException(notFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = UserRoleNotFoundException.class)
    public ResponseEntity<Object> handleUserRoleNotFoundException(UserRoleNotFoundException notFoundException) {

        ApiException apiException = new ApiException(notFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }



    @ExceptionHandler(value = EmployeeNotFoundException.class)
    public ResponseEntity<Object> handleEmployeeNotFoundException(EmployeeNotFoundException notFoundException) {

        ApiException apiException = new ApiException(notFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = EmployeeAlreadyExistException.class)
    public ResponseEntity<Object> handleEmployeeAlreadyExist(EmployeeAlreadyExistException notFoundException) {

        ApiException apiException = new ApiException(notFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidToken(InvalidTokenException invalidTokenException) {

        ApiException apiException = new ApiException(invalidTokenException.getMessage(), HttpStatus.FORBIDDEN, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = TokenNotFoundException.class)
    public ResponseEntity<Object> handleTokenNotFound(TokenNotFoundException tokenNotFoundException) {

        ApiException apiException = new ApiException(tokenNotFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = TokenExpiredException.class)
    public ResponseEntity<Object> handleExpiredToken(TokenExpiredException tokenExpiredException) {

        ApiException apiException = new ApiException(tokenExpiredException.getMessage(), HttpStatus.GONE, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.GONE);
    }

    @ExceptionHandler(value = UserAlreadyVerified.class)
    public ResponseEntity<Object> handelUserAlreadyVerified(UserAlreadyVerified userAlreadyVerified) {

        ApiException apiException = new ApiException(userAlreadyVerified.getMessage(), HttpStatus.CONFLICT, new Date());

        return new ResponseEntity<>(apiException, HttpStatus.CONFLICT);
    }


    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }
}
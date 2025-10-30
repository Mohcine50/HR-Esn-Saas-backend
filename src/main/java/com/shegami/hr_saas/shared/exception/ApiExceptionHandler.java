package com.shegami.hr_saas.shared.exception;

import com.shegami.hr_saas.modules.auth.exception.TenantNotFoundException;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.exception.UserRoleNotFoundException;
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


    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException notFoundException) {

        ApiException apiException = new ApiException(notFoundException.getMessage(), HttpStatus.NOT_FOUND, new Date());

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

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }
}
package com.groceryapp.exception;

import com.groceryapp.constant.common.ErrorCode;
import com.groceryapp.dto.response.common.ErrorResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponseWrapper> handleServiceException(ServiceException ex) {
        ErrorResponseWrapper response;

        if (!ex.getMessage().equals(ex.getErrorCode().getMessage())) {
            List<String> errors = List.of(ex.getMessage().split(", "));
            response = ErrorResponseWrapper.fromErrorCode(ex.getErrorCode(), errors);
        } else {
            response = ErrorResponseWrapper.fromErrorCode(ex.getErrorCode());
        }

        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseWrapper> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + ": " + errorMessage);
        });

        ErrorResponseWrapper response = ErrorResponseWrapper.fromErrorCode(ErrorCode.INVALID_REQUEST, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseWrapper> handleAllUncaughtException(Exception ex) {
        ErrorResponseWrapper response = ErrorResponseWrapper.builder().success(false)
                .message("An unexpected error occurred").errorCode(5000).build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        int code = errorCode.getCode();

        if (code >= 1000 && code < 5000) {
            if (code == ErrorCode.INVENTORY_ITEM_NOT_FOUND.getCode()) {
                return HttpStatus.NOT_FOUND;
            }
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

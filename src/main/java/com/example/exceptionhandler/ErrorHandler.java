package com.example.exceptionhandler;

import com.example.exception.IpAddressBlockedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(IpAddressBlockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    IpAddressBlockedReason onIpAddressBlockedException(IpAddressBlockedException iabe) {

        return new IpAddressBlockedReason(iabe.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse onConstraintValidationException(ConstraintViolationException e) {

        ValidationErrorResponse error = new ValidationErrorResponse();

        for (ConstraintViolation violation : e.getConstraintViolations()) {
            error.getViolations().add(new Violation(violation.getPropertyPath().toString(), violation.getMessage()));
        }
        return error;
    }
}

package com.k9x.infrastructure.in.rest.configuration.exception;

import com.k9x.domain.commons.exception.DomainException;
import com.k9x.infrastructure.in.rest.configuration.exception.error.CustomError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.ConnectException;
import java.util.Date;
import java.util.Locale;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;
    @Value("${k9x-backend.timeoutValue}")
    private String timeoutValue;

    public CustomExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(DomainException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleCustomException(DomainException ex, Locale locale) {

        CustomError error = new CustomError(ex.getId(),
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), ex.getDate(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InterruptedException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleInterruptedException(Locale locale) {

        CustomError error = new CustomError(String.valueOf(HttpStatus.REQUEST_TIMEOUT.value()),
                messageSource.getMessage("error.request_timeout", new String[]{timeoutValue}, locale), new Date().getTime(), HttpStatus.REQUEST_TIMEOUT.value());
        return new ResponseEntity<>(error, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(ConnectException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleConnectException(Locale locale) {

        CustomError error = new CustomError(String.valueOf(HttpStatus.NO_CONTENT.value()),
                messageSource.getMessage("error.module_not_available", null, locale),
                new Date().getTime(), HttpStatus.NO_CONTENT.value());
        return new ResponseEntity<>(error, HttpStatus.NO_CONTENT);
    }
}

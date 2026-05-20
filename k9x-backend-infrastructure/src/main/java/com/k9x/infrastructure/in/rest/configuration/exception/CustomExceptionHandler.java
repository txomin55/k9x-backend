package com.k9x.infrastructure.in.rest.configuration.exception;

import com.k9x.domain.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.infrastructure.in.rest.configuration.exception.error.CustomError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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

        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.PRECONDITION_FAILED.value());
        return new ResponseEntity<>(error, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(NotFoundResourceException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleNotFoundResourceException(NotFoundResourceException ex, Locale locale) {

        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedResourceException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleUnauthorizedResourceException(UnauthorizedResourceException ex, Locale locale) {

        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisciplineConfigurationMalformedException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleDisciplineConfigurationMalformedException(DisciplineConfigurationMalformedException ex, Locale locale) {
        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InterruptedException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleInterruptedException(Locale locale) {

        CustomError error = new CustomError(
                messageSource.getMessage("error.request_timeout", new String[]{timeoutValue}, locale), HttpStatus.REQUEST_TIMEOUT.value());
        return new ResponseEntity<>(error, HttpStatus.REQUEST_TIMEOUT);
    }
}

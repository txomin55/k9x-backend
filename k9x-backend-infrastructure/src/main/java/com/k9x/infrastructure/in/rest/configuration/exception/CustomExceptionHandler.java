package com.k9x.infrastructure.in.rest.configuration.exception;

import com.k9x.application.dogs.exceptions.DogIdentificationAlreadyExistsException;
import com.k9x.application.dogs.exceptions.DogOriginAlreadyExistsException;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.infrastructure.in.rest.configuration.exception.error.CustomError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    private final MessageSource messageSource;
    @Value("${k9x-backend.timeoutValue}")
    private String timeoutValue;

    public CustomExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(DomainException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleCustomException(DomainException ex, Locale locale) {

        log.warn("Domain exception [{}] args={}", ex.getId(), ex.getArgs(), ex);
        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.PRECONDITION_FAILED.value());
        return new ResponseEntity<>(error, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(NotFoundResourceException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleNotFoundResourceException(NotFoundResourceException ex, Locale locale) {

        log.warn("Not found resource [{}] args={}", ex.getId(), ex.getArgs());
        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedResourceException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleUnauthorizedResourceException(UnauthorizedResourceException ex, Locale locale) {

        log.warn("Unauthorized resource [{}] args={}", ex.getId(), ex.getArgs());
        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisciplineConfigurationMalformedException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleDisciplineConfigurationMalformedException(DisciplineConfigurationMalformedException ex, Locale locale) {
        log.warn("Discipline configuration malformed [{}] args={}", ex.getId(), ex.getArgs());
        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DogIdentificationAlreadyExistsException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleDogIdentificationAlreadyExistsException(DogIdentificationAlreadyExistsException ex, Locale locale) {
        log.warn("Dog identification already exists [{}] args={}", ex.getId(), ex.getArgs());
        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DogOriginAlreadyExistsException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleDogOriginAlreadyExistsException(DogOriginAlreadyExistsException ex, Locale locale) {
        log.warn("Dog origin already exists [{}] args={}", ex.getId(), ex.getArgs());
        CustomError error = new CustomError(
                messageSource.getMessage(ex.getId(), ex.getArgs(), locale), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InterruptedException.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleInterruptedException(InterruptedException ex, Locale locale) {

        log.error("Request timed out after {} ms", timeoutValue, ex);
        CustomError error = new CustomError(
                messageSource.getMessage("error.request_timeout", new String[]{timeoutValue}, locale), HttpStatus.REQUEST_TIMEOUT.value());
        return new ResponseEntity<>(error, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    final ResponseEntity<CustomError> handleUnexpectedException(Exception ex, Locale locale) {

        log.error("Unexpected exception", ex);
        CustomError error = new CustomError(
                messageSource.getMessage("error.internal", null, locale), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

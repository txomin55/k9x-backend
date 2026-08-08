package com.k9x.application.dogs.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class DogIdentificationAlreadyExistsException extends DomainException {

    public DogIdentificationAlreadyExistsException() {
        super(ErrorEnum.DOG_IDENTIFICATION_ALREADY_EXISTS);
    }
}

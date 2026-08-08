package com.k9x.application.dogs.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class DogOriginAlreadyExistsException extends DomainException {

    public DogOriginAlreadyExistsException() {
        super(ErrorEnum.DOG_ORIGIN_ALREADY_EXISTS);
    }
}

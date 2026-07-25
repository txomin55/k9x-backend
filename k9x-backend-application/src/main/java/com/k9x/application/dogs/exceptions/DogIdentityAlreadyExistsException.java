package com.k9x.application.dogs.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class DogIdentityAlreadyExistsException extends DomainException {

    public DogIdentityAlreadyExistsException() {
        super(ErrorEnum.DOG_IDENTITY_ALREADY_EXISTS);
    }
}

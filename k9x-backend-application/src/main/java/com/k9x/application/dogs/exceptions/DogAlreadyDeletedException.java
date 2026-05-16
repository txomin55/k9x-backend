package com.k9x.application.dogs.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class DogAlreadyDeletedException extends DomainException {

    public DogAlreadyDeletedException() {
        super(ErrorEnum.DOG_ALREADY_DELETED);
    }
}

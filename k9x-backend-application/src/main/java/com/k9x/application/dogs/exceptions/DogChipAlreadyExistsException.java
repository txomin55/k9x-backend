package com.k9x.application.dogs.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class DogChipAlreadyExistsException extends DomainException {

    public DogChipAlreadyExistsException() {
        super(ErrorEnum.DOG_CHIP_ALREADY_EXISTS);
    }
}

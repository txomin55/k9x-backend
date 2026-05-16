package com.k9x.application.dogs.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class DogNotFoundException extends NotFoundResourceException {

    public DogNotFoundException() {
        super(ErrorEnum.DOG_NOT_FOUND);
    }
}

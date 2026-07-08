package com.k9x.domain.disciplines.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class DisciplineNotFoundException extends NotFoundResourceException {

    public DisciplineNotFoundException() {
        super(ErrorEnum.DISCIPLINE_NOT_FOUND);
    }
}

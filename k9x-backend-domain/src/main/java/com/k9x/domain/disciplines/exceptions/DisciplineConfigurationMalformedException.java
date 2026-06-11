package com.k9x.domain.disciplines.exceptions;

import com.k9x.domain.exceptions.DomainException;

import com.k9x.domain.exceptions.error.ErrorEnum;

public class DisciplineConfigurationMalformedException extends DomainException {

    public DisciplineConfigurationMalformedException() {
        super(ErrorEnum.DISCIPLINE_CONFIGURATION_MALFORMED);
    }
}

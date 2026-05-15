package com.k9x.application.dogs.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class OwnerNonProvidedWhenOrganizer extends DomainException {
    public OwnerNonProvidedWhenOrganizer() {
        super(ErrorEnum.NO_OWNER_WHEN_NO_ORGANIZER);
    }
}

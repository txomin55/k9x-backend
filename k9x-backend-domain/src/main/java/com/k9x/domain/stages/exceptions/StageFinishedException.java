package com.k9x.domain.stages.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * Raised when an operation that only makes sense on a live stage is attempted on a finished one, e.g.
 * announcing something to the competitors of its events.
 */
public class StageFinishedException extends DomainException {

    public StageFinishedException() {
        super(ErrorEnum.STAGE_FINISHED);
    }
}

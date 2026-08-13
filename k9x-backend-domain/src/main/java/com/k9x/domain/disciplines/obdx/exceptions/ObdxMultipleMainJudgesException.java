package com.k9x.domain.disciplines.obdx.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

/**
 * More than one judge of an event was flagged as the main judge. The paper working booklet has a single
 * "juez principal / árbitro" box, so the event cannot have two.
 */
public class ObdxMultipleMainJudgesException extends DomainException {

    public ObdxMultipleMainJudgesException() {
        super(ErrorEnum.MULTIPLE_MAIN_JUDGES_IN_EVENT);
    }
}

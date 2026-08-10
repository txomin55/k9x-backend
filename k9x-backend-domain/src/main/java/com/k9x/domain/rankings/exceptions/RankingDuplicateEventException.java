package com.k9x.domain.rankings.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class RankingDuplicateEventException extends DomainException {

    public RankingDuplicateEventException() {
        super(ErrorEnum.RANKING_DUPLICATE_EVENT);
    }
}

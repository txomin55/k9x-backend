package com.k9x.domain.rankings.exceptions;

import com.k9x.domain.exceptions.NotFoundResourceException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class RankingNotFoundException extends NotFoundResourceException {

    public RankingNotFoundException() {
        super(ErrorEnum.RANKING_NOT_FOUND);
    }
}

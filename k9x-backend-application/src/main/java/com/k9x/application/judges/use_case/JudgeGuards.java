package com.k9x.application.judges.use_case;

import com.k9x.application.judges.exceptions.JudgeAlreadyDeletedException;
import com.k9x.application.judges.exceptions.JudgeNotFoundException;
import com.k9x.domain.judges.aggregates.Judge;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

/**
 * Shared write guards for judges, so the validation in update/delete lives in one place. A judge can only
 * be mutated by its creator while still active. The existence guard (not found) always applies.
 */
public final class JudgeGuards {

    private JudgeGuards() {}

    public static void assertMutableBy(Judge judge, String userId) {
        if (judge == null) {
            throw new JudgeNotFoundException();
        }
        if (judge.deletedAt() != null) {
            throw new JudgeAlreadyDeletedException();
        }
        if (!judge.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}

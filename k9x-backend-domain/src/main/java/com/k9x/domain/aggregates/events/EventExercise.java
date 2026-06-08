package com.k9x.domain.aggregates.events;

import java.util.List;

public record EventExercise(
        String exerciseId,
        Short position,
        List<String> tags
) {
}

package com.k9x.domain.events.valueobjects;

import java.util.List;

public record EventExercise(
        String exerciseId,
        Short position,
        List<String> tags,
        List<String> judges
) {
}

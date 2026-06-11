package com.k9x.application.competitions;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;

/**
 * Navigation helpers over the {@link CompetitionSnapshot} root aggregate. Children (stages, events) are only
 * accessed through their root, so every read first hydrates the competition and then locates the node.
 */
public final class CompetitionNavigator {

    private CompetitionNavigator() {}

    public static StageSnapshot findStage(CompetitionSnapshot competition, String stageId) {
        if (competition == null || competition.stages() == null) {
            return null;
        }
        return competition.stages().stream()
                .filter(s -> s.id().equals(stageId))
                .findFirst()
                .orElse(null);
    }

    public static EventSnapshot findEvent(CompetitionSnapshot competition, String eventId) {
        if (competition == null || competition.stages() == null) {
            return null;
        }
        return competition.stages().stream()
                .filter(s -> s.events() != null)
                .flatMap(s -> s.events().stream())
                .filter(e -> e.id().equals(eventId))
                .findFirst()
                .orElse(null);
    }

    public static StageSnapshot findStageOfEvent(CompetitionSnapshot competition, String eventId) {
        if (competition == null || competition.stages() == null) {
            return null;
        }
        return competition.stages().stream()
                .filter(s -> s.events() != null && s.events().stream().anyMatch(e -> e.id().equals(eventId)))
                .findFirst()
                .orElse(null);
    }
}

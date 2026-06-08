package com.k9x.application.competitions;

import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

/**
 * Navigation helpers over the {@link Competition} root aggregate. Children (stages, events) are only
 * accessed through their root, so every read first hydrates the competition and then locates the node.
 */
public final class CompetitionNavigator {

    private CompetitionNavigator() {}

    public static Stage findStage(Competition competition, String stageId) {
        if (competition == null || competition.stages() == null) {
            return null;
        }
        return competition.stages().stream()
                .filter(s -> s.id().equals(stageId))
                .findFirst()
                .orElse(null);
    }

    public static Event findEvent(Competition competition, String eventId) {
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

    public static Stage findStageOfEvent(Competition competition, String eventId) {
        if (competition == null || competition.stages() == null) {
            return null;
        }
        return competition.stages().stream()
                .filter(s -> s.events() != null && s.events().stream().anyMatch(e -> e.id().equals(eventId)))
                .findFirst()
                .orElse(null);
    }
}

package com.k9x.domain.competitions.aggregates;

import com.k9x.domain.competitions.commands.*;
import com.k9x.domain.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionCannotBeDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.domain.competitions.status.CompetitionStatus;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.*;
import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.UtcDates;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.stages.exceptions.*;
import com.k9x.domain.stages.status.StageStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Write-side root aggregate. Wraps the immutable {@link CompetitionSnapshot} read snapshot and is the single
 * entry point for mutations: each mutation method enforces the domain invariants against the snapshot
 * and records a typed {@link CompetitionChange}. The persistence adapter later replays
 * {@link #pendingChanges()} to emit only the affected SQL.
 *
 * <p>Invariants are validated against the original loaded snapshot, so chaining interdependent
 * mutations on a single aggregate instance is not supported (no in-scope operation needs it).
 */
public final class CompetitionAggregate {

    private final CompetitionSnapshot snapshot;
    private final List<CompetitionChange> changes = new ArrayList<>();

    private CompetitionAggregate(CompetitionSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Wraps an existing competition; throws when it does not exist.
     */
    public static CompetitionAggregate of(CompetitionSnapshot snapshot) {
        if (snapshot == null) {
            throw new CompetitionNotFoundException();
        }
        return new CompetitionAggregate(snapshot);
    }

    /**
     * Seeds a brand-new competition (no snapshot to load yet).
     */
    public static CompetitionAggregate createNew(String id, String name, String creator, long now) {
        CompetitionAggregate aggregate = new CompetitionAggregate(null);
        aggregate.changes.add(new CompetitionCreated(id, name, creator, now));
        return aggregate;
    }

    private static String normalizeDiscipline(String disciplineId) {
        if (disciplineId == null) {
            throw new DisciplineConfigurationMalformedException();
        }
        try {
            return Discipline.valueOf(disciplineId.toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException e) {
            throw new DisciplineConfigurationMalformedException();
        }
    }

    // ---- CompetitionSnapshot mutations -------------------------------------------------------------------

    public List<CompetitionChange> pendingChanges() {
        return List.copyOf(changes);
    }

    public void update(CompetitionUpdateData data, String userId, long now) {
        assertCompetitionMutableBy(userId);

        changes.add(new CompetitionUpdated(snapshot.id(), data.name(), data.description(), data.country(),
                data.address(), data.coordAlt(), data.coordLong(), now));
    }

    // ---- StageSnapshot mutations -------------------------------------------------------------------------

    public void delete(String userId, long now) {
        assertCompetitionMutableBy(userId);
        assertCompetitionDeletable(now);

        changes.add(new CompetitionDeleted(snapshot.id(), now));
        if (snapshot.stages() != null) {
            snapshot.stages().stream()
                    .filter(s -> s.deletedAt() == null)
                    .forEach(s -> {
                        changes.add(new StageDeleted(s.id(), now));
                        cascadeDeleteEvents(s, now);
                    });
        }
    }

    public void createStage(NewStageData data, String userId, long now) {
        assertCompetitionMutableBy(userId);

        changes.add(new StageCreated(data.id(), data.name(), snapshot.id(), data.dateFrom(), data.dateTo(),
                userId, now));
    }

    public void renameStage(String stageId, StageUpdateData data, String userId, long now) {
        StageSnapshot stage = requireActiveStage(stageId);
        assertStageOwnedBy(stage, userId);
        assertCompetitionMutableBy(userId);

        changes.add(new StageRenamed(stageId, data.name(), data.dateFrom(), data.dateTo(), now));
    }

    // ---- EventSnapshot mutations -------------------------------------------------------------------------

    public void deleteStage(String stageId, String userId, long now) {
        StageSnapshot stage = requireActiveStage(stageId);
        assertStageOwnedBy(stage, userId);
        assertCompetitionMutableBy(userId);
        assertStageDeletable(stage, now);

        changes.add(new StageDeleted(stageId, now));
        cascadeDeleteEvents(stage, now);
    }

    public void createEvent(NewEventData data, String userId, long now) {
        StageSnapshot stage = requireActiveStage(data.stageId());
        assertStageOwnedBy(stage, userId);

        String discipline = normalizeDiscipline(data.discipline());
        changes.add(new EventCreated(data.id(), data.name(), data.stageId(), discipline, userId, now));
    }

    public void deleteEvent(String eventId, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId);
        assertEventDeletable(event);
        StageSnapshot stage = findStageOfEvent(eventId);

        assert stage != null;
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        assertStageOwnedBy(stage, userId);
        changes.add(new EventDeleted(eventId, now));
    }

    public void enrollDog(String eventId, String dogId, long now) {
        requireActiveEvent(eventId);
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;

        if (stage.dateTo() < now) {
            throw new StageExpiredException();
        }
        changes.add(new DogEnrolled(eventId, dogId, now));
    }

    public void updateObdxEventInfo(String eventId, ObdxEventUpdateData data, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId);

        if (!event.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
        changes.add(new ObdxEventInfoUpdated(eventId, data.name(), data.configurationId(), data.scoreCalculation(),
                data.enrollmentDeadline(), data.competitors(), data.exercises(), data.judges(), now));
    }

    /**
     * Flags (or clears) a competitor as not competing. A not-competing competitor is treated as settled by
     * {@link EventSnapshot#status()}, i.e. equivalent to one who has finished competing. Marking a competitor
     * that is already not competing is rejected with {@link CompetitorAlreadyNotCompetingException}.
     */
    public void updateCompetitorNotCompeting(String eventId, String dogId, boolean notCompeting, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId);

        if (!event.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
        EventCompetitor competitor = findCompetitor(event, dogId);
        if (notCompeting && competitor.notCompeting()) {
            throw new CompetitorAlreadyNotCompetingException();
        }
        changes.add(new CompetitorNotCompetingUpdated(eventId, dogId, notCompeting, now));
    }

    public void updateScore(String eventId, ScoreUpdateData data, long now) {
        requireActiveEvent(eventId);
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;
        if (UtcDates.isBeforeUtcDay(now, stage.dateFrom())) {
            throw new StageNotStartedException();
        }
        if (stage.dateTo() < now) {
            throw new StageExpiredException();
        }
        changes.add(new ScoreUpdated(eventId, data.judgeId(), data.exerciseId(), data.dogId(), data.score(), now));
    }

    // ---- invariants & navigation -----------------------------------------------------------------

    private void assertCompetitionMutableBy(String userId) {
        if (snapshot.deletedAt() != null) {
            throw new CompetitionAlreadyDeletedException();
        }
        if (!snapshot.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }

    /**
     * A competition can only be deleted while every one of its (active) stages is still deletable, i.e. each
     * stage and all of its events are in the CREATED state. Deleting it cascades the soft-delete to those
     * stages and their events.
     */
    private void assertCompetitionDeletable(long now) {
        CompetitionStatus status = snapshot.status(now);
        if (status == CompetitionStatus.STARTED || status == CompetitionStatus.FINISHED) {
            throw new CompetitionCannotBeDeletedException();
        }
        if (snapshot.stages() != null && !snapshot.stages().stream()
                .filter(s -> s.deletedAt() == null)
                .allMatch(s -> isStageDeletable(s, now))) {
            throw new CompetitionCannotBeDeletedException();
        }
    }

    private StageSnapshot requireActiveStage(String stageId) {
        StageSnapshot stage = findStage(stageId);
        if (stage == null) {
            throw new StageNotFoundException();
        }
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        return stage;
    }

    private void assertStageOwnedBy(StageSnapshot stage, String userId) {
        if (!stage.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertStageDeletable(StageSnapshot stage, long now) {
        if (!isStageDeletable(stage, now)) {
            throw new StageCannotBeDeletedException();
        }
    }

    /**
     * A stage is deletable only while it has not started or finished and every one of its (active) events is
     * still in the CREATED state. Deleting it cascades the soft-delete to those events.
     */
    private boolean isStageDeletable(StageSnapshot stage, long now) {
        StageStatus status = stage.status(now);
        if (status == StageStatus.STARTED || status == StageStatus.FINISHED) {
            return false;
        }
        if (stage.events() == null) {
            return true;
        }
        return stage.events().stream()
                .filter(e -> e.deletedAt() == null)
                .allMatch(e -> e.status() == EventStatus.CREATED);
    }

    /**
     * Records an {@link EventDeleted} for every still-active event of the stage, so deleting a stage (or the
     * whole competition) propagates the soft-delete down to its events.
     */
    private void cascadeDeleteEvents(StageSnapshot stage, long now) {
        if (stage.events() == null) {
            return;
        }
        stage.events().stream()
                .filter(e -> e.deletedAt() == null)
                .forEach(e -> changes.add(new EventDeleted(e.id(), now)));
    }

    private EventSnapshot requireActiveEvent(String eventId) {
        EventSnapshot event = findEvent(eventId);
        if (event == null) {
            throw new EventNotFoundException();
        }
        if (event.deletedAt() != null) {
            throw new EventAlreadyDeletedException();
        }
        return event;
    }

    private void assertEventDeletable(EventSnapshot event) {
        EventStatus status = event.status();
        if (status == EventStatus.STARTED || status == EventStatus.FINISHED) {
            throw new EventCannotBeDeletedException();
        }
    }

    private StageSnapshot findStage(String stageId) {
        if (snapshot.stages() == null) {
            return null;
        }
        return snapshot.stages().stream()
                .filter(s -> s.id().equals(stageId))
                .findFirst()
                .orElse(null);
    }

    private EventSnapshot findEvent(String eventId) {
        if (snapshot.stages() == null) {
            return null;
        }
        return snapshot.stages().stream()
                .filter(s -> s.events() != null)
                .flatMap(s -> s.events().stream())
                .filter(e -> e.id().equals(eventId))
                .findFirst()
                .orElse(null);
    }

    private EventCompetitor findCompetitor(EventSnapshot event, String dogId) {
        if (event.competitors() == null) {
            throw new CompetitorNotFoundException();
        }
        return event.competitors().stream()
                .filter(c -> c.dogId().equals(dogId))
                .findFirst()
                .orElseThrow(CompetitorNotFoundException::new);
    }

    private StageSnapshot findStageOfEvent(String eventId) {
        if (snapshot.stages() == null) {
            return null;
        }
        return snapshot.stages().stream()
                .filter(s -> s.events() != null && s.events().stream().anyMatch(e -> e.id().equals(eventId)))
                .findFirst()
                .orElse(null);
    }
}

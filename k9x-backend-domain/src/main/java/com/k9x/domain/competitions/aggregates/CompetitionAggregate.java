package com.k9x.domain.competitions.aggregates;

import com.k9x.domain.competitions.commands.*;
import com.k9x.domain.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionCannotBeDeletedException;
import com.k9x.domain.competitions.exceptions.CompetitionCannotBeUpdatedException;
import com.k9x.domain.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.domain.competitions.status.CompetitionStatus;
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
import java.util.Objects;

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

    private static final String SMOKE_TEST_PREFIX = "--SMOKE--";
    private static final String SMOKE_TEST_CREATOR = "k9x.support@gmail.com";

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
        return Discipline.fromStored(disciplineId).name();
    }

    // ---- CompetitionSnapshot mutations -------------------------------------------------------------------

    public List<CompetitionChange> pendingChanges() {
        return List.copyOf(changes);
    }

    /**
     * Whether the given user created the event. Used by the application layer to authorize event-creator
     * operations (e.g. scoring) without duplicating the snapshot navigation. Unknown events return
     * {@code false}.
     */
    public boolean isEventCreatedBy(String eventId, String userId) {
        EventSnapshot event = findEvent(eventId);
        return event != null && event.creator().equals(userId);
    }

    /**
     * The event's creator, or {@code null} when the event is unknown. Used by the application layer to
     * address a post-enrollment notification without re-reading the aggregate.
     */
    public String eventCreator(String eventId) {
        EventSnapshot event = findEvent(eventId);
        return event == null ? null : event.creator();
    }

    /**
     * The event's display name, or {@code null} when the event is unknown.
     */
    public String eventName(String eventId) {
        EventSnapshot event = findEvent(eventId);
        return event == null ? null : event.name();
    }

    /**
     * The id of the stage that owns the event, or {@code null} when the event is unknown.
     */
    public String stageIdOfEvent(String eventId) {
        StageSnapshot stage = findStageOfEvent(eventId);
        return stage == null ? null : stage.id();
    }

    /**
     * The display name of the stage that owns the event, or {@code null} when the event is unknown.
     */
    public String stageNameOfEvent(String eventId) {
        StageSnapshot stage = findStageOfEvent(eventId);
        return stage == null ? null : stage.name();
    }

    /**
     * The display name of an active stage, for use as notification metadata. Unlike
     * {@link #stageNameOfEvent(String)} this resolves the stage directly instead of through one of its
     * events, and rejects unknown or soft-deleted stages rather than returning {@code null}.
     */
    public String activeStageName(String stageId) {
        StageSnapshot stage = findStage(stageId);
        if (stage == null) {
            throw new StageNotFoundException();
        }
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        return stage.name();
    }

    /**
     * Authorizes addressing a notification to an event's competitors: the event must be active, must belong
     * to the given stage, and must have been created by the user. Navigating the aggregate here keeps the
     * application layer from re-deriving the stage↔event relationship.
     */
    public void assertEventNotifiableBy(String eventId, String stageId, String userId) {
        EventSnapshot event = requireActiveEvent(eventId, userId);
        StageSnapshot stage = findStageOfEvent(eventId);
        if (stage == null || !stage.id().equals(stageId)) {
            throw new EventNotInStageException();
        }
        if (!event.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }

    /**
     * The id of the competition this aggregate wraps.
     */
    public String competitionId() {
        return snapshot == null ? null : snapshot.id();
    }

    /**
     * The display name of the competition this aggregate wraps.
     */
    public String competitionName() {
        return snapshot == null ? null : snapshot.name();
    }

    public void update(CompetitionUpdateData data, String userId, long now) {
        assertCompetitionMutableBy(userId);
        assertCompetitionUpdatable(now);

        changes.add(new CompetitionUpdated(snapshot.id(), data.name(), data.description(), data.country(),
                data.address(), data.coordAlt(), data.coordLong(), now));
    }

    // ---- StageSnapshot mutations -------------------------------------------------------------------------

    public void delete(String userId, long now) {
        assertCompetitionMutableBy(userId);
        if (!isSmokeTestCompetition()) {
            assertCompetitionDeletable(now);
        }

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
        assertCompetitionUpdatable(now);
        assertStageDateRange(data.dateFrom(), data.dateTo());

        changes.add(new StageCreated(data.id(), data.name(), snapshot.id(), data.dateFrom(), data.dateTo(),
                userId, now));
    }

    public void updateStage(String stageId, StageUpdateData data, String userId, long now) {
        StageSnapshot stage = requireActiveStage(stageId, userId);
        assertStageOwnedBy(stage, userId);
        assertCompetitionMutableBy(userId);
        assertStageUpdatable(stage, now);
        assertStageDateRange(data.dateFrom(), data.dateTo());
        assertEventDeadlinesBeforeStageStart(stage, data.dateFrom());

        changes.add(new StageUpdated(stageId, data.name(), data.dateFrom(), data.dateTo(), now));
    }

    /**
     * Re-dating a stage must keep every (active) event's enrollment deadline before the new start day —
     * the same invariant enforced when the deadline is set. Otherwise moving a future stage onto today
     * would leave enrollment open on an already-running stage (Enroll and Classification showing at once).
     */
    private void assertEventDeadlinesBeforeStageStart(StageSnapshot stage, Long dateFrom) {
        if (stage.events() == null) {
            return;
        }
        boolean violated = stage.events().stream()
                .filter(e -> e.deletedAt() == null)
                .anyMatch(e -> e.enrollmentDeadline() != null
                        && !UtcDates.isBeforeUtcDay(e.enrollmentDeadline(), dateFrom));
        if (violated) {
            throw new EnrollmentDeadlineAfterStageStartException();
        }
    }

    // ---- EventSnapshot mutations -------------------------------------------------------------------------

    public void deleteStage(String stageId, String userId, long now) {
        StageSnapshot stage = requireActiveStage(stageId, userId);
        assertStageOwnedBy(stage, userId);
        assertCompetitionMutableBy(userId);
        assertStageDeletable(stage, now);

        changes.add(new StageDeleted(stageId, now));
        cascadeDeleteEvents(stage, now);
    }

    public void createEvent(NewEventData data, String userId, long now) {
        StageSnapshot stage = requireActiveStage(data.stageId(), userId);
        assertStageOwnedBy(stage, userId);
        assertStageAcceptsNewEvents(stage, now);

        String discipline = normalizeDiscipline(data.discipline());
        changes.add(new EventCreated(data.id(), data.name(), data.stageId(), discipline, userId, now));
    }

    public void deleteEvent(String eventId, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId, userId);
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;
        assertEventDeletable(event, stage, now);
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        assertStageOwnedBy(stage, userId);
        changes.add(new EventDeleted(eventId, now));
    }

    public void enrollDog(String eventId, String dogId, boolean bih, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId, userId);
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;

        if (UtcDates.isAfterUtcDay(now, stage.dateTo())) {
            throw new StageExpiredException();
        }
        if (!stage.enrollmentOpened(event, now)) {
            throw new EnrollmentClosedException();
        }
        if (isDogEnrolled(event, dogId)) {
            throw new DogAlreadyEnrolledException();
        }
        changes.add(new DogEnrolled(eventId, dogId, bih, nextStartNumber(event), now));
    }

    /**
     * New enrollments are appended after every already-enrolled competitor, so a dog always joins at the
     * back of the line rather than defaulting to start number 0/null.
     */
    private short nextStartNumber(EventSnapshot event) {
        if (event.competitors() == null || event.competitors().isEmpty()) {
            return 1;
        }
        return (short) (event.competitors().stream()
                .map(EventCompetitor::startNumber)
                .filter(Objects::nonNull)
                .mapToInt(Short::intValue)
                .max()
                .orElse(0) + 1);
    }

    public void updateObdxEventInfo(String eventId, ObdxEventUpdateData data, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId, userId);

        if (!event.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;
        assertEventUpdatable(event, stage, now);
        assertEnrollmentDeadlineBeforeStageStart(data.enrollmentDeadline(), stage);
        changes.add(new ObdxEventInfoUpdated(eventId, data.name(), data.configurationId(), data.scoreCalculation(),
                data.enrollmentDeadline(), data.competitors(), data.exercises(), data.judges(), now, data.awards(),
                data.rankScore(), data.international()));
    }

    /**
     * Flags (or clears) a competitor as not competing. A not-competing competitor is treated as settled by
     * {@link EventSnapshot#status(long, long)}, i.e. equivalent to one who has finished competing. Marking a
     * competitor that is already not competing is rejected with {@link CompetitorAlreadyNotCompetingException}.
     */
    public void updateCompetitorNotCompeting(String eventId, String dogId, boolean notCompeting, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId, userId);

        if (!event.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
        EventCompetitor competitor = findCompetitor(event, dogId);
        if (notCompeting && competitor.notCompeting()) {
            throw new CompetitorAlreadyNotCompetingException();
        }
        changes.add(new CompetitorNotCompetingUpdated(eventId, dogId, notCompeting, now));
    }

    public void updateScore(String eventId, ScoreUpdateData data, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId, userId);
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;
        if (UtcDates.isBeforeUtcDay(now, stage.dateFrom())) {
            throw new StageNotStartedException();
        }
        if (UtcDates.isAfterUtcDay(now, stage.dateTo())) {
            throw new StageExpiredException();
        }
        assertJudgeAssignedToExercise(event, data.exerciseId(), data.judgeId());
        if (event.isDisqualified(data.dogId())) {
            throw new CompetitorDisqualifiedException();
        }
        if (event.isNotCompeting(data.dogId())) {
            throw new CompetitorNotCompetingException();
        }
        changes.add(new ScoreUpdated(eventId, data.judgeId(), data.exerciseId(), data.dogId(), data.score(), now));
    }

    public void registerYellowCard(String eventId, YellowCardData data, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId, userId);
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;
        if (UtcDates.isBeforeUtcDay(now, stage.dateFrom())) {
            throw new StageNotStartedException();
        }
        if (UtcDates.isAfterUtcDay(now, stage.dateTo())) {
            throw new StageExpiredException();
        }
        assertJudgeAssignedToExercise(event, data.exerciseId(), data.judgeId());
        if (hasYellowCard(event, data)) {
            throw new YellowCardAlreadyRegisteredException();
        }
        changes.add(new YellowCardRegistered(eventId, data.judgeId(), data.exerciseId(), data.dogId(), now));

        /*
         * A second yellow card disqualifies the competitor exactly like a red card (see
         * EventSnapshot#isDisqualified), so it stamps one automatically in the same exercise/judge as the
         * card that triggered it, unless one is already registered.
         */
        if (event.yellowCardCount(data.dogId()) + 1 >= 2 && !event.hasRedCard(data.dogId())) {
            changes.add(new RedCardRegistered(eventId, data.judgeId(), data.exerciseId(), data.dogId(), now));
        }
    }

    public void registerRedCard(String eventId, RedCardData data, String userId, long now) {
        EventSnapshot event = requireActiveEvent(eventId, userId);
        StageSnapshot stage = findStageOfEvent(eventId);
        assert stage != null;
        if (UtcDates.isBeforeUtcDay(now, stage.dateFrom())) {
            throw new StageNotStartedException();
        }
        if (UtcDates.isAfterUtcDay(now, stage.dateTo())) {
            throw new StageExpiredException();
        }
        assertJudgeAssignedToExercise(event, data.exerciseId(), data.judgeId());
        if (event.hasRedCard(data.dogId())) {
            throw new RedCardAlreadyRegisteredException();
        }
        changes.add(new RedCardRegistered(eventId, data.judgeId(), data.exerciseId(), data.dogId(), now));
    }

    /**
     * A score or card can only be recorded for an exercise/judge pair the event actually recognises: the judge
     * must be assigned to that exercise in the event configuration. Otherwise it would be persisted but never
     * surfaced by the classification (which only reads scores/cards for assigned judge+exercise pairs).
     */
    private void assertJudgeAssignedToExercise(EventSnapshot event, String exerciseId, String judgeId) {
        boolean assigned = event.exercises() != null && event.exercises().stream()
                .filter(e -> Objects.equals(e.exerciseId(), exerciseId))
                .anyMatch(e -> e.judges() != null && e.judges().contains(judgeId));
        if (!assigned) {
            throw new ExerciseJudgeNotAssignedException(judgeId, exerciseId);
        }
    }

    private boolean hasYellowCard(EventSnapshot event, YellowCardData data) {
        if (event.scores() == null) {
            return false;
        }
        return event.scores().stream()
                .anyMatch(s -> s.yellowCard() != null
                        && s.judgeId().equals(data.judgeId())
                        && s.exerciseId().equals(data.exerciseId())
                        && s.dogId().equals(data.dogId()));
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
     * A competition can only be edited (and have stages added) while it is still in the CREATED state: once any
     * of its stages is under way (TO_START/STARTED) or everything has finished, its configuration is locked.
     */
    private void assertCompetitionUpdatable(long now) {
        if (snapshot.status(now) != CompetitionStatus.CREATED) {
            throw new CompetitionCannotBeUpdatedException();
        }
    }

    /**
     * The smoke-test suite creates real data (prefixed {@code --SMOKE--}, owned by the support account) and
     * cleans it up afterwards, including competitions that scoring has already moved to STARTED. Those are
     * exempt from the status-based delete restriction so the cleanup can remove everything it created.
     */
    private boolean isSmokeTestCompetition() {
        return snapshot.name() != null && snapshot.name().startsWith(SMOKE_TEST_PREFIX)
                && SMOKE_TEST_CREATOR.equals(snapshot.creator());
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

    private StageSnapshot requireActiveStage(String stageId, String userId) {
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

    /**
     * A stage's {@code dateTo} must fall on at least the same UTC day as its {@code dateFrom}: a stage cannot
     * end before the day it starts.
     */
    private void assertStageDateRange(Long dateFrom, Long dateTo) {
        if (UtcDates.isBeforeUtcDay(dateTo, dateFrom)) {
            throw new StageDateToBeforeDateFromException();
        }
    }

    private void assertStageDeletable(StageSnapshot stage, long now) {
        if (!isStageDeletable(stage, now)) {
            throw new StageCannotBeDeletedException();
        }
    }

    /**
     * A stage can only be edited while it is still in the CREATED state: once it is TO_START, STARTED or
     * FINISHED its configuration is locked.
     */
    private void assertStageUpdatable(StageSnapshot stage, long now) {
        if (stage.status(now) != StageStatus.CREATED) {
            throw new StageCannotBeUpdatedException();
        }
    }

    /**
     * A new event can only be added to a stage that is still in the CREATED state: once the stage is
     * TO_START, STARTED or FINISHED its line-up is locked.
     */
    private void assertStageAcceptsNewEvents(StageSnapshot stage, long now) {
        if (stage.status(now) != StageStatus.CREATED) {
            throw new EventCannotBeCreatedException();
        }
    }

    /**
     * A stage is deletable only while it is still in the CREATED state. A CREATED stage cannot yet hold any
     * non-CREATED event, so this also guarantees its events are all deletable. Deleting it cascades the
     * soft-delete to those events.
     */
    private boolean isStageDeletable(StageSnapshot stage, long now) {
        return stage.status(now) == StageStatus.CREATED;
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

    private EventSnapshot requireActiveEvent(String eventId, String userId) {
        EventSnapshot event = findEvent(eventId);
        if (event == null) {
            throw new EventNotFoundException();
        }
        if (event.deletedAt() != null) {
            throw new EventAlreadyDeletedException();
        }
        return event;
    }

    private void assertEventDeletable(EventSnapshot event, StageSnapshot stage, long now) {
        if (event.status(now, stage.dateTo()) != EventStatus.CREATED) {
            throw new EventCannotBeDeletedException();
        }
    }

    /**
     * The enrollment deadline must fall at least the day before the stage starts: enrollment has to close
     * before the stage's {@code dateFrom} day. A null deadline (no deadline set) is always allowed.
     */
    private void assertEnrollmentDeadlineBeforeStageStart(Long enrollmentDeadline, StageSnapshot stage) {
        if (enrollmentDeadline != null && !UtcDates.isBeforeUtcDay(enrollmentDeadline, stage.dateFrom())) {
            throw new EnrollmentDeadlineAfterStageStartException();
        }
    }

    /**
     * An event's configuration can only be edited while the event itself is still in the CREATED state, i.e.
     * before any score is recorded on it (or its stage's {@code dateTo} day has passed). Scoring a running
     * stage is a separate concern handled in the score path.
     */
    private void assertEventUpdatable(EventSnapshot event, StageSnapshot stage, long now) {
        if (event.status(now, stage.dateTo()) != EventStatus.CREATED) {
            throw new EventCannotBeUpdatedException();
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

    private boolean isDogEnrolled(EventSnapshot event, String dogId) {
        return event.competitors() != null
                && event.competitors().stream().anyMatch(c -> c.dogId().equals(dogId));
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

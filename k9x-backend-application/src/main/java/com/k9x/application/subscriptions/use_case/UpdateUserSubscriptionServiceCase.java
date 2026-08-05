package com.k9x.application.subscriptions.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.subscriptions.port.CreateUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.port.UpdateUserSubscriptionPersistencePort;
import com.k9x.application.subscriptions.port.payload.UpdateUserSubscriptionPersistencePayload;
import com.k9x.application.subscriptions.use_case.command.UpdateUserSubscriptionCommand;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.subscriptions.SubscriptionKind;

import java.util.List;

/**
 * Toggles a set of subscriptions of the authenticated user. Generic by design: the kind decides which list
 * the ids are added to or removed from, so a new subscribable resource does not need a new endpoint. The
 * whole set is toggled in one transaction, which is what the UI needs — one tap on a stage's bell covers
 * every event of that stage.
 *
 * <p>Subscribing is validated against each resource — an event that has already finished emits no further
 * notifications, so it cannot be subscribed to. Unsubscribing is never validated: a user must always be
 * able to drop a subscription, whatever state the resource ended up in.
 *
 * <p>The subscriptions record is created before the toggle (idempotently) because users registered before
 * this feature existed have no record yet; both writes share the use case's transaction.
 */
public class UpdateUserSubscriptionServiceCase implements TransactionalUseCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final CreateUserSubscriptionsPersistencePort createUserSubscriptionsPersistencePort;
    private final UpdateUserSubscriptionPersistencePort updateUserSubscriptionPersistencePort;

    public UpdateUserSubscriptionServiceCase(
            GetCompetitionPersistencePort getCompetitionPersistencePort,
            CreateUserSubscriptionsPersistencePort createUserSubscriptionsPersistencePort,
            UpdateUserSubscriptionPersistencePort updateUserSubscriptionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.createUserSubscriptionsPersistencePort = createUserSubscriptionsPersistencePort;
        this.updateUserSubscriptionPersistencePort = updateUserSubscriptionPersistencePort;
    }

    public void updateUserSubscription(UpdateUserSubscriptionCommand command, String userId) {
        SubscriptionKind kind = SubscriptionKind.of(command.kind());
        if (command.ids() == null || command.ids().isEmpty()) {
            return;
        }
        if (command.subscribe()) {
            assertSubscribable(kind, command.ids());
        }
        createUserSubscriptionsPersistencePort.createUserSubscriptions(userId);
        updateUserSubscriptionPersistencePort.updateUserSubscription(
                UpdateUserSubscriptionPersistencePayload.from(command, kind, userId));
    }

    private void assertSubscribable(SubscriptionKind kind, List<String> ids) {
        switch (kind) {
            case EVENT -> ids.forEach(this::assertEventSubscribable);
        }
    }

    private void assertEventSubscribable(String eventId) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.assertEventSubscribable(eventId, DateUtils.nowUtcMillis());
    }
}

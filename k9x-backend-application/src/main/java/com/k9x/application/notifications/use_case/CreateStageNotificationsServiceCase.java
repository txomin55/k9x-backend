package com.k9x.application.notifications.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.notifications.port.GetEventRecipientsPersistencePort;
import com.k9x.application.notifications.port.PushNotifier;
import com.k9x.application.notifications.port.SaveEventNotificationPersistencePort;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.application.notifications.port.payload.SaveEventNotificationPersistencePayload;
import com.k9x.application.notifications.port.payload.SaveNotificationPersistencePayload;
import com.k9x.application.notifications.use_case.command.CreateStageNotificationCommand;
import com.k9x.application.notifications.valueobjects.NotificationType;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.stages.exceptions.StageNotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lets the organizer that created a stage's events broadcast a free-text announcement to their competitors.
 *
 * <p>Recipients are derived from the event rosters at send time (see
 * {@link GetEventRecipientsPersistencePort}); there is no subscription state to keep in sync. A user with
 * several dogs across the announcement's events is notified once, not once per dog or per event.
 *
 * <p>The announcement, its event links and every inbox row are written inside this use case's transaction,
 * so an inbox entry is guaranteed for each recipient. Only the push delivery happens outside it —
 * {@link PushNotifier#deliver} defers to after the commit — so a delivery failure can neither roll the
 * transaction back nor lose the inbox row.
 */
public class CreateStageNotificationsServiceCase implements TransactionalUseCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveEventNotificationPersistencePort saveEventNotificationPersistencePort;
    private final GetEventRecipientsPersistencePort getEventRecipientsPersistencePort;
    private final SaveNotificationPersistencePort saveNotificationPersistencePort;
    private final PushNotifier pushNotifier;

    public CreateStageNotificationsServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                               SaveEventNotificationPersistencePort saveEventNotificationPersistencePort,
                                               GetEventRecipientsPersistencePort getEventRecipientsPersistencePort,
                                               SaveNotificationPersistencePort saveNotificationPersistencePort,
                                               PushNotifier pushNotifier) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveEventNotificationPersistencePort = saveEventNotificationPersistencePort;
        this.getEventRecipientsPersistencePort = getEventRecipientsPersistencePort;
        this.saveNotificationPersistencePort = saveNotificationPersistencePort;
        this.pushNotifier = pushNotifier;
    }

    public void createStageNotifications(String stageId, List<CreateStageNotificationCommand> commands,
                                         String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        if (commands == null || commands.isEmpty()) {
            return;
        }
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(stageId);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));

        // Validate everything before the first write, so a rejected request leaves no partial announcement
        // behind and sends no pushes.
        String stageName = competition.activeStageName(stageId);
        for (String eventId : distinctEventIds(commands)) {
            competition.assertEventNotifiableBy(eventId, stageId, userId);
        }

        List<Recipient> pending = new ArrayList<>();
        for (CreateStageNotificationCommand command : commands) {
            saveEventNotificationPersistencePort.save(SaveEventNotificationPersistencePayload.from(command));
            PushNotification notification = new PushNotification(NotificationType.EVENT_NOTIFICATION, Map.of(
                    "stage_id", stageId,
                    "stage_name", stageName,
                    "content", command.content()));
            for (String recipientUserId : getEventRecipientsPersistencePort.getRecipientIds(command.eventIds())) {
                saveNotificationPersistencePort.save(
                        SaveNotificationPersistencePayload.from(recipientUserId, notification));
                pending.add(new Recipient(recipientUserId, notification));
            }
        }

        // Deferred to after commit by the notifier, so nothing is pushed for a transaction that rolls back.
        pending.forEach(recipient -> pushNotifier.deliver(recipient.userId(), recipient.notification()));
    }

    private Set<String> distinctEventIds(List<CreateStageNotificationCommand> commands) {
        Set<String> eventIds = new LinkedHashSet<>();
        commands.stream()
                .filter(command -> command.eventIds() != null)
                .forEach(command -> eventIds.addAll(command.eventIds()));
        return eventIds;
    }

    private record Recipient(String userId, PushNotification notification) {
    }
}

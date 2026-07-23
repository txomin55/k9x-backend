package com.k9x.application.events.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.obdx.use_case.BihGuards;
import com.k9x.application.events.obdx.use_case.command.EnrollObdxEventCommand;
import com.k9x.application.notifications.port.PushNotifier;
import com.k9x.application.notifications.valueobjects.NotificationType;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.events.exceptions.EventNotFoundException;

import java.util.Map;

public class EnrollEventServiceCase implements TransactionalUseCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    private final GetDogPersistencePort getDogPersistencePort;
    private final PushNotifier pushNotifier;

    public EnrollEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  SaveCompetitionPersistencePort saveCompetitionPersistencePort,
                                  GetDogPersistencePort getDogPersistencePort,
                                  PushNotifier pushNotifier) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
        this.getDogPersistencePort = getDogPersistencePort;
        this.pushNotifier = pushNotifier;
    }

    public void enrollEvent(String eventId, EnrollObdxEventCommand command, String userId) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        Dog dog = getDogPersistencePort.getDog(command.dogId());
        BihGuards.assertBihAllowedForSex(command.bih(), dog);
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.enrollDog(eventId, command.dogId(), command.bih(), userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
        notifyCreator(competition, eventId, command.dogId(), userId);
    }

    private void notifyCreator(CompetitionAggregate competition, String eventId, String dogId, String enrollerUserId) {
        String creator = competition.eventCreator(eventId);
        // Don't notify the creator when they enroll into their own event.
        if (creator == null || creator.equals(enrollerUserId)) {
            return;
        }
        // Ids + display names: the frontend maps type + metadata to the displayed text and the URL.
        pushNotifier.notify(creator, new PushNotification(NotificationType.NEW_ENROLL, Map.of(
                "competition_id", competition.competitionId(),
                "competition_name", competition.competitionName(),
                "stage_id", competition.stageIdOfEvent(eventId),
                "stage_name", competition.stageNameOfEvent(eventId),
                "event_id", eventId,
                "event_name", competition.eventName(eventId),
                "dog_id", dogId)));
    }
}

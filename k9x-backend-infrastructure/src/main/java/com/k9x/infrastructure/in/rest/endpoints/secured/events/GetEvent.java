package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.GetEventServiceCase;
import com.k9x.application.events.use_case.dto.FetchEventConfigurationDTO;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.events.use_case.dto.FetchEventExerciseDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsFetchOneApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;

public class GetEvent implements SecuredEventsFetchOneApiDelegate {

    private final GetEventServiceCase getEventServiceCase;
    private final UserInfoDTO userDetails;
    private final MessageSource messageSource;

    public GetEvent(GetEventServiceCase getEventServiceCase, UserInfoDTO userDetails, MessageSource messageSource) {
        this.getEventServiceCase = getEventServiceCase;
        this.userDetails = userDetails;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<EventDetailResponseDTO> getOneEventSecured(String id, List<String> ids) {
        FetchEventDetailDTO event = getEventServiceCase.getEvent(id, userDetails.getEmail(), userDetails.isOrganizer());

        EventDetailResponseDTO response = new EventDetailResponseDTO();
        if (event.obdx() != null) {
            response.setObdx(toObdx(event));
        }

        return ResponseEntity.ok(response);
    }

    private ObdxEventDetailResponseDTO toObdx(FetchEventDetailDTO event) {
        FetchObdxEventDTO obdx = event.obdx();
        return new ObdxEventDetailResponseDTO(
                obdx.id(),
                new IdNameDTO(obdx.stageId(), obdx.stageName()),
                obdx.name(),
                obdx.status(),
                resolveDiscipline(obdx.discipline()),
                mapCompetitors(event.competitors()),
                mapExercises(event.exercises()),
                mapConfiguration(event.configuration()),
                mapJudges(event.judges()),
                obdx.enrollmentDeadline()
        );
    }

    private List<EventCompetitorResponseDTO> mapCompetitors(List<FetchObdxEventCompetitorDTO> competitors) {
        return competitors.stream()
                .map(c -> new EventCompetitorResponseDTO(
                        c.owner(),
                        c.handler(),
                        c.dogIdentity(),
                        c.team(),
                        c.country(),
                        c.position() != null ? c.position().intValue() : null,
                        c.status(),
                        c.breed(),
                        new IdNameDTO(c.dogName(), c.dogId()),
                        c.bih(),
                        // notCompeting is not surfaced here: `status` already resolves to NOT_COMPETING
                        // via EventCompetitorStatus.of(...) for the event detail endpoint.
                        null,
                        // scoresAllowed is not computed for the event detail endpoint, only for collections.
                        null))
                .toList();
    }

    private List<EventExerciseDetailResponseDTO> mapExercises(List<FetchEventExerciseDTO> exercises) {
        return exercises.stream()
                .map(e -> new EventExerciseDetailResponseDTO(e.id(), e.name(), e.position(), e.tags()))
                .toList();
    }

    private List<EventJudgeDetailResponseDTO> mapJudges(List<FetchObdxEventJudgeDTO> judges) {
        return judges.stream()
                .map(j -> new EventJudgeDetailResponseDTO(j.judgeId(), j.judgeName(), j.collectorEmail()))
                .toList();
    }

    private EventConfigurationDetailResponseDTO mapConfiguration(FetchEventConfigurationDTO configuration) {
        if (configuration == null) {
            return null;
        }
        FederationConfigurationResponseDTO federation = configuration.federation() == null ? null
                : new FederationConfigurationResponseDTO(configuration.federation().id(),
                configuration.federation().name(), configuration.federation().country());
        return new EventConfigurationDetailResponseDTO(configuration.id(), configuration.name(), federation);
    }

    private IdNameDTO resolveDiscipline(String disciplineId) {
        if (disciplineId == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "discipline." + disciplineId.toUpperCase(Locale.ROOT) + ".name", null, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, disciplineId);
    }
}

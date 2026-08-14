package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.GetEventServiceCase;
import com.k9x.application.events.use_case.dto.FetchEventConfigurationDTO;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.events.use_case.dto.FetchEventExerciseDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.SecuredEventsFetchOneApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetEvent implements SecuredEventsFetchOneApiDelegate {

    private final GetEventServiceCase getEventServiceCase;
    private final UserInfoDTO userDetails;
    private final ReferenceNameResolver referenceNames;

    public GetEvent(GetEventServiceCase getEventServiceCase, UserInfoDTO userDetails,
                    ReferenceNameResolver referenceNames) {
        this.getEventServiceCase = getEventServiceCase;
        this.userDetails = userDetails;
        this.referenceNames = referenceNames;
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
                referenceNames.discipline(obdx.discipline()),
                mapCompetitors(event.competitors()),
                mapExercises(event.exercises()),
                mapConfiguration(event.configuration()),
                mapJudges(event.judges()),
                obdx.enrollmentDeadline(),
                obdx.scoreCalculation() == null ? null : obdx.scoreCalculation().name(),
                obdx.awards().stream().map(a -> new IdNameDTO(a, a)).toList(),
                obdx.commissioner(),
                obdx.category() == null ? null : obdx.category().name(),
                obdx.source().name()
        );
    }

    private List<EventCompetitorResponseDTO> mapCompetitors(List<FetchObdxEventCompetitorDTO> competitors) {
        return competitors.stream()
                .map(c -> new EventCompetitorResponseDTO(
                        c.owner(),
                        c.handler(),
                        c.dogOrigin(),
                        c.team(),
                        c.country(),
                        c.startNumber() != null ? c.startNumber().intValue() : null,
                        c.competitorNumber() != null ? c.competitorNumber().intValue() : null,
                        c.status(),
                        referenceNames.breed(c.breed()),
                        new IdNameDTO(c.dogName(), c.dogIdentification()),
                        c.bih(),
                        c.primer(),
                        c.reserve(),
                        // notCompeting is not surfaced here: `status` already resolves to NOT_COMPETING
                        // via EventCompetitorStatus.of(...) for the event detail endpoint.
                        null,
                        // scoresAllowed is not computed for the event detail endpoint, only for collections.
                        null))
                .toList();
    }

    private List<EventExerciseDetailResponseDTO> mapExercises(List<FetchEventExerciseDTO> exercises) {
        return exercises.stream()
                .map(e -> new EventExerciseDetailResponseDTO(e.id(), e.name(), e.position(), e.tags(),
                        e.judges().stream().map(j -> new IdNameDTO(j.judgeName(), j.judgeId())).toList()))
                .toList();
    }

    private List<EventJudgeDetailResponseDTO> mapJudges(List<FetchObdxEventJudgeDTO> judges) {
        return judges.stream()
                .map(j -> new EventJudgeDetailResponseDTO(j.judgeId(), j.judgeName(), j.collectorEmail(), j.mainJudge()))
                .toList();
    }

    private EventConfigurationDetailResponseDTO mapConfiguration(FetchEventConfigurationDTO configuration) {
        if (configuration == null) {
            return null;
        }
        FederationConfigurationResponseDTO federation = configuration.federation() == null ? null
                : new FederationConfigurationResponseDTO(configuration.federation().id(),
                configuration.federation().name());
        return new EventConfigurationDetailResponseDTO(configuration.id(), configuration.name(), federation);
    }
}

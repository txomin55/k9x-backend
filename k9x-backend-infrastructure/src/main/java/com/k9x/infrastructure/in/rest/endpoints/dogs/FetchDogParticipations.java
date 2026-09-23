package com.k9x.infrastructure.in.rest.endpoints.dogs;

import com.k9x.application.dogs.use_case.GetDogParticipationsServiceCase;
import com.k9x.application.dogs.use_case.dto.DogParticipationDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.DogsFetchParticipationsApiDelegate;
import com.k9x.oas.stub.model.DogParticipationResponseDTO;
import com.k9x.oas.stub.model.DogParticipationYearResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Public history of a dog's events. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}.
 */
public class FetchDogParticipations implements DogsFetchParticipationsApiDelegate {

    private final GetDogParticipationsServiceCase getDogParticipationsServiceCase;
    private final ReferenceNameResolver referenceNames;

    public FetchDogParticipations(GetDogParticipationsServiceCase getDogParticipationsServiceCase,
                                  ReferenceNameResolver referenceNames) {
        this.getDogParticipationsServiceCase = getDogParticipationsServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<List<DogParticipationYearResponseDTO>> fetchDogParticipations(String identification) {
        return ResponseEntity.ok(getDogParticipationsServiceCase.getParticipations(identification).stream()
                .map(year -> new DogParticipationYearResponseDTO(
                        year.year(),
                        year.participations().stream().map(this::toResponse).toList()))
                .toList());
    }

    /**
     * A restricted competition keeps where the dog placed but not what it scored, the same line
     * {@code WithheldScores} draws for the public classification.
     */
    private DogParticipationResponseDTO toResponse(DogParticipationDTO participation) {
        boolean restricted = participation.restricted();
        return new DogParticipationResponseDTO(
                new IdNameDTO(participation.eventName(), participation.eventId()),
                participation.stageId(),
                participation.stageDateFrom(),
                referenceNames.country(participation.country()),
                participation.position() == null ? null : participation.position().intValue(),
                restricted ? null : participation.totalScore(),
                restricted ? null : participation.rankScore(),
                restricted);
    }
}

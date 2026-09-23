package com.k9x.infrastructure.in.rest.endpoints.dogs;

import com.k9x.application.dogs.rank.use_case.GetDogIndexTimelineServiceCase;
import com.k9x.application.dogs.rank.use_case.dto.DogIndexEventDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogIndexTimelineDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogIndexEventDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.DogsFetchIndexApiDelegate;
import com.k9x.oas.stub.model.DogIndexEventResponseDTO;
import com.k9x.oas.stub.model.DogIndexPointResponseDTO;
import com.k9x.oas.stub.model.DogIndexTimelineResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

/**
 * Public chart of a dog's K9X index. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}.
 */
public class FetchDogIndex implements DogsFetchIndexApiDelegate {

    private final GetDogIndexTimelineServiceCase getDogIndexTimelineServiceCase;
    private final ReferenceNameResolver referenceNames;

    public FetchDogIndex(GetDogIndexTimelineServiceCase getDogIndexTimelineServiceCase,
                         ReferenceNameResolver referenceNames) {
        this.getDogIndexTimelineServiceCase = getDogIndexTimelineServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<DogIndexTimelineResponseDTO> fetchDogIndex(String identification) {
        DogIndexTimelineDTO timeline = getDogIndexTimelineServiceCase.getIndexTimeline(identification);
        return ResponseEntity.ok(new DogIndexTimelineResponseDTO(
                timeline.events().stream().map(this::toResponse).toList(),
                timeline.curve().stream()
                        .map(point -> new DogIndexPointResponseDTO(point.timestamp(), point.index()))
                        .toList(),
                timeline.freshnessDegradationFrom()));
    }

    /**
     * A restricted competition keeps where the dog placed but not what it scored, the same line
     * {@code WithheldScores} draws for the public classification. The index itself stays: it is public already.
     */
    private DogIndexEventResponseDTO toResponse(DogIndexEventDTO event) {
        FetchDogIndexEventDTO result = event.result();
        boolean restricted = result.restricted();
        return new DogIndexEventResponseDTO(
                new IdNameDTO(result.eventName(), result.eventId()),
                result.stageId(),
                referenceNames.discipline(result.discipline()),
                referenceNames.country(result.country()),
                result.appliesAt(),
                restricted ? null : result.rankScore(),
                restricted ? null : result.totalScore(),
                result.position() == null ? null : result.position().intValue(),
                event.indexAfter(),
                restricted);
    }
}

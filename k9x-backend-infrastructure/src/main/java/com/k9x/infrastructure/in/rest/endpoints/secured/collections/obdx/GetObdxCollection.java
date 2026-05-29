package com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx;

import com.k9x.application.collections.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCollectionsFetchOneApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetObdxCollection implements SecuredCollectionsFetchOneApiDelegate {

    private final GetObdxCollectionServiceCase getObdxCollectionServiceCase;
    private final UserInfoDTO userDetails;

    public GetObdxCollection(GetObdxCollectionServiceCase getObdxCollectionServiceCase, UserInfoDTO userDetails) {
        this.getObdxCollectionServiceCase = getObdxCollectionServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<CollectionResponseDTO> fetchOneCollection(String id) {
        FetchCollectionDetailDTO detail = getObdxCollectionServiceCase.getCollection(id, userDetails.getEmail());

        Map<String, String> judgeNames = detail.judges().stream()
                .collect(Collectors.toMap(FetchCollectionJudgeWithCollectorDTO::judgeId,
                        FetchCollectionJudgeWithCollectorDTO::judgeName));

        List<CompetitorScoresResponseDTO> competitors = detail.competitors().stream()
                .map(comp -> new CompetitorScoresResponseDTO(
                        detail.exercises().stream()
                                .map(ex -> new ExerciseScoresResponseDTO(
                                        new ExerciseResponseDTO(ex.exerciseId(), ex.exerciseId(),
                                                ex.position() != null ? ex.position().intValue() : null),
                                        detail.scores().stream()
                                                .filter(s -> s.dogId().equals(comp.dogId())
                                                        && s.exerciseId().equals(ex.exerciseId()))
                                                .map(s -> new CollectionScoreResponseDTO(s.score(),
                                                        new IdNameDTO(judgeNames.get(s.judgeId()), s.judgeId())))
                                                .toList()
                                ))
                                .toList(),
                        new EventCompetitorResponseDTO(
                                comp.owner(),
                                comp.dogIdentity(),
                                comp.team(),
                                comp.country(),
                                comp.position() != null ? comp.position().intValue() : null,
                                comp.status(),
                                new IdNameDTO(comp.dogName(), comp.dogId())
                        )
                ))
                .toList();

        return ResponseEntity.ok(new CollectionResponseDTO(
                new ScoresConfigurationResponseDTO(detail.allowedValues(), detail.configurationId()),
                new ObdxCompetitorsScoresResponseDTO(competitors),
                new IdNameDTO(detail.configurationId(), detail.configurationId())
        ));
    }
}

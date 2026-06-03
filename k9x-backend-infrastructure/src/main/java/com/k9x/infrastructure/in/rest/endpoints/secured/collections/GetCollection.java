package com.k9x.infrastructure.in.rest.endpoints.secured.collections;

import com.k9x.application.collections.use_case.GetObdxCollectionServiceCase;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCollectionsFetchOneObdxApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetCollection implements SecuredCollectionsFetchOneObdxApiDelegate {

    private final GetObdxCollectionServiceCase getObdxCollectionServiceCase;
    private final UserInfoDTO userDetails;
    private final MessageSource messageSource;

    public GetCollection(GetObdxCollectionServiceCase getObdxCollectionServiceCase, UserInfoDTO userDetails,
                         MessageSource messageSource) {
        this.getObdxCollectionServiceCase = getObdxCollectionServiceCase;
        this.userDetails = userDetails;
        this.messageSource = messageSource;
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
                                        new ExerciseResponseDTO(ex.exerciseId(), resolveTranslation(ex.exerciseId()),
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
                                comp.breed(),
                                new IdNameDTO(comp.dogName(), comp.dogId())
                        )
                ))
                .toList();

        return ResponseEntity.ok(new CollectionResponseDTO(
                new ScoresConfigurationResponseDTO(detail.allowedValues(), resolveTranslation(detail.configurationId())),
                new ObdxCompetitorsScoresResponseDTO(competitors),
                resolveDiscipline(detail.discipline())
        ));
    }

    private String resolveTranslation(String key) {
        if (key == null) {
            return null;
        }
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    private IdNameDTO resolveDiscipline(String disciplineId) {
        if (disciplineId == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "discipline." + disciplineId + ".name", null, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, disciplineId);
    }
}

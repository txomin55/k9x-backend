package com.k9x.infrastructure.in.rest.endpoints.secured.collections;

import com.k9x.application.collections.obdx.use_case.dto.FetchCollectionCompetitorScoresDTO;
import com.k9x.application.collections.use_case.GetCollectionServiceCase;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredCollectionsFetchOneApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;

public class GetCollection implements SecuredCollectionsFetchOneApiDelegate {

    private final GetCollectionServiceCase getCollectionServiceCase;
    private final UserInfoDTO userDetails;
    private final MessageSource messageSource;

    public GetCollection(GetCollectionServiceCase getCollectionServiceCase, UserInfoDTO userDetails,
                         MessageSource messageSource) {
        this.getCollectionServiceCase = getCollectionServiceCase;
        this.userDetails = userDetails;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<CollectionResponseDTO> fetchOneCollection(String id) {
        FetchCollectionDetailDTO detail = getCollectionServiceCase.getCollection(id, userDetails.getEmail());

        return ResponseEntity.ok(new CollectionResponseDTO(
                new ScoresConfigurationResponseDTO(detail.allowedValues(), resolveTranslation(detail.configurationId())),
                detail.obdx() == null ? null
                        : new ObdxCompetitorsScoresResponseDTO(mapCompetitors(detail.obdx().competitors())),
                resolveDiscipline(detail.discipline())
        ));
    }

    private List<CompetitorScoresResponseDTO> mapCompetitors(List<FetchCollectionCompetitorScoresDTO> competitors) {
        return competitors.stream()
                .map(comp -> new CompetitorScoresResponseDTO(
                        comp.exercises().stream()
                                .map(ex -> new ExerciseScoresResponseDTO(
                                        new ExerciseResponseDTO(ex.exerciseId(), resolveTranslation(ex.exerciseId()),
                                                ex.position() != null ? ex.position().intValue() : null),
                                        ex.scores().stream()
                                                .map(s -> new CollectionScoreResponseDTO(s.score(),
                                                        new IdNameDTO(s.judgeName(), s.judgeId())))
                                                .toList()
                                ))
                                .toList(),
                        new EventCompetitorResponseDTO(
                                comp.competitor().owner(),
                                comp.competitor().handler(),
                                comp.competitor().dogIdentity(),
                                comp.competitor().team(),
                                comp.competitor().country(),
                                comp.competitor().position() != null ? comp.competitor().position().intValue() : null,
                                comp.competitor().status(),
                                comp.competitor().breed(),
                                new IdNameDTO(comp.competitor().dogName(), comp.competitor().dogId()),
                                comp.competitor().bih()
                        )
                ))
                .toList();
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
                "discipline." + disciplineId.toUpperCase(Locale.ROOT) + ".name", null, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, disciplineId);
    }
}

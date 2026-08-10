package com.k9x.infrastructure.in.rest.endpoints.secured.collections;

import com.k9x.application.collections.obdx.use_case.dto.FetchCollectionCompetitorScoresDTO;
import com.k9x.application.collections.use_case.GetCollectionServiceCase;
import com.k9x.application.collections.use_case.dto.FetchCollectionDetailDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.SecuredCollectionsFetchOneApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetCollection implements SecuredCollectionsFetchOneApiDelegate {

    private final GetCollectionServiceCase getCollectionServiceCase;
    private final UserInfoDTO userDetails;
    private final MessageSource messageSource;
    private final ReferenceNameResolver referenceNames;

    public GetCollection(GetCollectionServiceCase getCollectionServiceCase, UserInfoDTO userDetails,
                         MessageSource messageSource, ReferenceNameResolver referenceNames) {
        this.getCollectionServiceCase = getCollectionServiceCase;
        this.userDetails = userDetails;
        this.messageSource = messageSource;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<CollectionResponseDTO> fetchOneCollection(String id) {
        FetchCollectionDetailDTO detail = getCollectionServiceCase.getCollection(id, userDetails.getEmail());

        return ResponseEntity.ok(new CollectionResponseDTO(
                detail.competitionName(),
                detail.eventName(),
                new ScoresConfigurationResponseDTO(detail.allowedValues(), resolveTranslation(detail.configurationId())),
                detail.obdx() == null ? null
                        : new ObdxCompetitorsScoresResponseDTO(mapCompetitors(detail.obdx().competitors())),
                referenceNames.discipline(detail.discipline())
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
                                comp.competitor().dogOrigin(),
                                comp.competitor().team(),
                                comp.competitor().country(),
                                comp.competitor().startNumber() != null ? comp.competitor().startNumber().intValue() : null,
                                comp.competitor().competitorNumber() != null ? comp.competitor().competitorNumber().intValue() : null,
                                comp.competitor().status(),
                                referenceNames.breed(comp.competitor().breed()),
                                new IdNameDTO(comp.competitor().dogName(), comp.competitor().dogIdentification()),
                                comp.competitor().bih(),
                                comp.competitor().primer(),
                                comp.competitor().reserve(),
                                comp.competitor().notCompeting(),
                                comp.competitor().scoresAllowed()
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
}

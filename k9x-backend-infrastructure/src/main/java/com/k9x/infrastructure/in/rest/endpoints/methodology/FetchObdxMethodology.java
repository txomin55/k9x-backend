package com.k9x.infrastructure.in.rest.endpoints.methodology;

import com.k9x.application.methodology.use_case.GetObdxMethodologyServiceCase;
import com.k9x.application.methodology.use_case.dto.LocalizedTextDTO;
import com.k9x.application.methodology.use_case.dto.ObdxMethodologyDTO;
import com.k9x.oas.stub.api.MethodologyFetchObdxApiDelegate;
import com.k9x.oas.stub.model.MethodologyCategoryResponseDTO;
import com.k9x.oas.stub.model.MethodologyCompetitorRangeResponseDTO;
import com.k9x.oas.stub.model.MethodologyFederationResponseDTO;
import com.k9x.oas.stub.model.MethodologyGlobalScaleResponseDTO;
import com.k9x.oas.stub.model.MethodologyGradeCategoryResponseDTO;
import com.k9x.oas.stub.model.MethodologyGradeCategoryTierResponseDTO;
import com.k9x.oas.stub.model.MethodologyGradeResponseDTO;
import com.k9x.oas.stub.model.MethodologyLocalizedTextResponseDTO;
import com.k9x.oas.stub.model.MethodologyMeritContextResponseDTO;
import com.k9x.oas.stub.model.MethodologyMeritCurveResponseDTO;
import com.k9x.oas.stub.model.MethodologyMeritParametersResponseDTO;
import com.k9x.oas.stub.model.MethodologyPointResponseDTO;
import com.k9x.oas.stub.model.MethodologyQualificationResponseDTO;
import com.k9x.oas.stub.model.MethodologyScaleRangeResponseDTO;
import com.k9x.oas.stub.model.MethodologyScoreRangeResponseDTO;
import com.k9x.oas.stub.model.MethodologySeriesResponseDTO;
import com.k9x.oas.stub.model.MethodologyTierResponseDTO;
import com.k9x.oas.stub.model.ObdxMethodologyResponseDTO;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

/**
 * Public OBDX methodology. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}. The answer
 * depends neither on the user nor on the request, only on the deployed rules, so it is cacheable for a day.
 */
public class FetchObdxMethodology implements MethodologyFetchObdxApiDelegate {

    static final CacheControl CACHE_CONTROL = CacheControl.maxAge(Duration.ofDays(1)).cachePublic();

    private final GetObdxMethodologyServiceCase getObdxMethodologyServiceCase;

    public FetchObdxMethodology(GetObdxMethodologyServiceCase getObdxMethodologyServiceCase) {
        this.getObdxMethodologyServiceCase = getObdxMethodologyServiceCase;
    }

    @Override
    public ResponseEntity<ObdxMethodologyResponseDTO> fetchObdxMethodology() {
        ObdxMethodologyDTO methodology = getObdxMethodologyServiceCase.getMethodology();
        return ResponseEntity.ok().cacheControl(CACHE_CONTROL).body(new ObdxMethodologyResponseDTO(
                methodology.schemaVersion(),
                new MethodologyGlobalScaleResponseDTO(methodology.globalScale().min(), methodology.globalScale().max(),
                        methodology.globalScale().ranges().stream()
                                .map(range -> new MethodologyScaleRangeResponseDTO(
                                        MethodologyScaleRangeResponseDTO.LetterEnum.fromValue(range.letter()),
                                        range.min(), range.max()))
                                .toList()),
                methodology.tiers().stream()
                        .map(tier -> new MethodologyTierResponseDTO(tier.tier(), competitors(tier.competitors())))
                        .toList(),
                methodology.categories().stream()
                        .map(category -> new MethodologyCategoryResponseDTO(
                                MethodologyCategoryResponseDTO.IdEnum.fromValue(category.id()),
                                localized(category.name()), category.championship()))
                        .toList(),
                methodology.federations().stream().map(FetchObdxMethodology::federation).toList(),
                meritCurve(methodology.meritCurve())));
    }

    private static MethodologyFederationResponseDTO federation(ObdxMethodologyDTO.Federation federation) {
        return new MethodologyFederationResponseDTO(federation.id(), federation.name(),
                federation.grades().stream()
                        .map(grade -> new MethodologyGradeResponseDTO(grade.id(), localized(grade.name()),
                                range(grade.band()),
                                grade.possibleLetters().stream()
                                        .map(MethodologyGradeResponseDTO.PossibleLettersEnum::fromValue)
                                        .toList(),
                                grade.categories().stream().map(FetchObdxMethodology::gradeCategory).toList()))
                        .toList());
    }

    private static MethodologyGradeCategoryResponseDTO gradeCategory(ObdxMethodologyDTO.GradeCategory category) {
        return new MethodologyGradeCategoryResponseDTO(
                MethodologyGradeCategoryResponseDTO.IdEnum.fromValue(category.id()),
                range(category.subBand()), category.fixed(),
                category.tiers().stream()
                        .map(tier -> new MethodologyGradeCategoryTierResponseDTO(tier.tier(),
                                competitors(tier.competitors()), tier.rankScore(),
                                MethodologyGradeCategoryTierResponseDTO.LetterEnum.fromValue(tier.letter())))
                        .toList());
    }

    private static MethodologyMeritCurveResponseDTO meritCurve(ObdxMethodologyDTO.MeritCurve meritCurve) {
        ObdxMethodologyDTO.MeritContext context = meritCurve.context();
        return new MethodologyMeritCurveResponseDTO(
                new MethodologyMeritContextResponseDTO(context.configuration(),
                        MethodologyMeritContextResponseDTO.CategoryEnum.fromValue(context.category()),
                        context.eventScore(), context.gradeFloor(), context.maxScore(),
                        context.qualifications().stream()
                                .map(qualification -> new MethodologyQualificationResponseDTO(qualification.id(),
                                        qualification.nameEn(), qualification.score(), qualification.top()))
                                .toList(),
                        new MethodologyMeritParametersResponseDTO(context.parameters().unlockPct(),
                                context.parameters().kneeShare(), context.parameters().floorBelowFirstQualification())),
                meritCurve.series().stream()
                        .map(series -> new MethodologySeriesResponseDTO(
                                MethodologySeriesResponseDTO.IdEnum.fromValue(series.id()),
                                series.points().stream()
                                        .map(point -> new MethodologyPointResponseDTO(point.x(), point.y()))
                                        .toList()))
                        .toList());
    }

    private static MethodologyScoreRangeResponseDTO range(ObdxMethodologyDTO.ScoreRange range) {
        return new MethodologyScoreRangeResponseDTO(range.min(), range.max());
    }

    private static MethodologyCompetitorRangeResponseDTO competitors(ObdxMethodologyDTO.CompetitorRange range) {
        return new MethodologyCompetitorRangeResponseDTO(range.min(), range.max());
    }

    static MethodologyLocalizedTextResponseDTO localized(LocalizedTextDTO text) {
        return new MethodologyLocalizedTextResponseDTO(text.es(), text.en());
    }
}

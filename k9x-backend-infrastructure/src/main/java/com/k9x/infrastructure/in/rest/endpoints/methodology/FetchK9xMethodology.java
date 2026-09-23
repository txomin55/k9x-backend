package com.k9x.infrastructure.in.rest.endpoints.methodology;

import com.k9x.application.methodology.use_case.GetK9xMethodologyServiceCase;
import com.k9x.application.methodology.use_case.dto.K9xMethodologyDTO;
import com.k9x.oas.stub.api.MethodologyFetchK9xApiDelegate;
import com.k9x.oas.stub.model.K9xMethodologyResponseDTO;
import com.k9x.oas.stub.model.MethodologyDecayAnchorResponseDTO;
import com.k9x.oas.stub.model.MethodologyDecayCurvesResponseDTO;
import com.k9x.oas.stub.model.MethodologyDecayFloorResponseDTO;
import com.k9x.oas.stub.model.MethodologyDecaySeriesResponseDTO;
import com.k9x.oas.stub.model.MethodologyExampleDogResponseDTO;
import com.k9x.oas.stub.model.MethodologyExampleEventResponseDTO;
import com.k9x.oas.stub.model.MethodologyExampleIndexPointResponseDTO;
import com.k9x.oas.stub.model.MethodologyExampleResultResponseDTO;
import com.k9x.oas.stub.model.MethodologyIndexExampleResponseDTO;
import com.k9x.oas.stub.model.MethodologyIndexParametersResponseDTO;
import com.k9x.oas.stub.model.MethodologyScoreRangeResponseDTO;
import com.k9x.oas.stub.model.MethodologySlotFillingCaseResponseDTO;
import com.k9x.oas.stub.model.MethodologySlotFillingResponseDTO;
import org.springframework.http.ResponseEntity;

import static com.k9x.infrastructure.in.rest.endpoints.methodology.FetchObdxMethodology.CACHE_CONTROL;
import static com.k9x.infrastructure.in.rest.endpoints.methodology.FetchObdxMethodology.localized;

/**
 * Public K9X index methodology. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}. Cacheable
 * for a day, like {@link FetchObdxMethodology}.
 */
public class FetchK9xMethodology implements MethodologyFetchK9xApiDelegate {

    private final GetK9xMethodologyServiceCase getK9xMethodologyServiceCase;

    public FetchK9xMethodology(GetK9xMethodologyServiceCase getK9xMethodologyServiceCase) {
        this.getK9xMethodologyServiceCase = getK9xMethodologyServiceCase;
    }

    @Override
    public ResponseEntity<K9xMethodologyResponseDTO> fetchK9xMethodology() {
        K9xMethodologyDTO methodology = getK9xMethodologyServiceCase.getMethodology();
        K9xMethodologyDTO.IndexExample example = methodology.indexExample();
        K9xMethodologyDTO.IndexParameters parameters = example.parameters();
        return ResponseEntity.ok().cacheControl(CACHE_CONTROL).body(new K9xMethodologyResponseDTO(
                methodology.schemaVersion(),
                new MethodologyScoreRangeResponseDTO(methodology.indexScale().min(), methodology.indexScale().max()),
                new MethodologyDecayCurvesResponseDTO(methodology.decayCurves().stream()
                        .map(series -> new MethodologyDecaySeriesResponseDTO(
                                MethodologyDecaySeriesResponseDTO.IdEnum.fromValue(series.id()),
                                series.plateauMonths(),
                                new MethodologyDecayFloorResponseDTO(series.floorFromMonth(), series.floorValue()),
                                series.anchors().stream()
                                        .map(anchor -> new MethodologyDecayAnchorResponseDTO(anchor.month(), anchor.weight()))
                                        .toList()))
                        .toList()),
                new MethodologyIndexExampleResponseDTO(example.formula(),
                        new MethodologyIndexParametersResponseDTO(parameters.slots(), parameters.prior(),
                                localized(parameters.priorReference()), parameters.filler(),
                                parameters.provisionalIfResultsBelow()),
                        new MethodologySlotFillingResponseDTO(example.slotFilling().stream()
                                .map(slotCase -> new MethodologySlotFillingCaseResponseDTO(slotCase.id(),
                                        slotCase.results(), slotCase.slots(), slotCase.level()))
                                .toList()),
                        example.dogs().stream().map(FetchK9xMethodology::dog).toList())));
    }

    private static MethodologyExampleDogResponseDTO dog(K9xMethodologyDTO.ExampleDog dog) {
        return new MethodologyExampleDogResponseDTO(dog.id(), localized(dog.name()),
                dog.results().stream()
                        .map(result -> new MethodologyExampleResultResponseDTO(result.month(), result.score()))
                        .toList(),
                dog.series().stream()
                        .map(point -> new MethodologyExampleIndexPointResponseDTO(point.month(), point.index()))
                        .toList(),
                dog.events().stream()
                        .map(event -> new MethodologyExampleEventResponseDTO(event.month(), event.index(),
                                event.score(), event.label()))
                        .toList());
    }
}

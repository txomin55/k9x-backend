package com.k9x.application.rankings.use_case;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationCellDTO;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationDTO;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationEventDTO;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationGroupDTO;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationMemberDTO;
import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.rankings.aggregates.Ranking;
import com.k9x.domain.rankings.results.RankingAggregation;
import com.k9x.domain.rankings.results.RankingCompetitorResult;
import com.k9x.domain.rankings.results.RankingEventResults;
import com.k9x.domain.rankings.results.RankingGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Public, unauthenticated read of a ranking's results.
 *
 * <p>Read-only, so it is not a {@code TransactionalUseCase}, and unlike {@link GetRankingServiceCase} it is
 * neither organizer-gated nor creator-scoped: anyone can see a published ranking.
 *
 * <p>The per-event classifications are obtained through {@link GetEventClassificationServiceCase} rather
 * than recomputed here. That is what makes the fan-out affordable: finished events are served from the
 * persisted snapshot and live ones from the short-lived classification cache, exactly like the single-event
 * endpoint. The aggregation itself is a pure domain function.
 */
public class GetRankingClassificationServiceCase {

    private final GetRankingPersistencePort getRankingPersistencePort;
    private final GetEventClassificationServiceCase getEventClassificationServiceCase;

    public GetRankingClassificationServiceCase(
            GetRankingPersistencePort getRankingPersistencePort,
            GetEventClassificationServiceCase getEventClassificationServiceCase) {
        this.getRankingPersistencePort = getRankingPersistencePort;
        this.getEventClassificationServiceCase = getEventClassificationServiceCase;
    }

    public Optional<FetchRankingClassificationDTO> getRankingClassification(String id) {
        Ranking ranking = getRankingPersistencePort.getRanking(id);
        if (ranking == null) {
            return Optional.empty();
        }

        Map<String, FetchClassificationDTO> classificationsByEvent = new LinkedHashMap<>();
        for (String eventId : ranking.eventIds()) {
            classification(eventId).ifPresent(found -> classificationsByEvent.put(eventId, found));
        }

        List<String> eventIds = List.copyOf(classificationsByEvent.keySet());
        List<RankingGroup> groups = RankingAggregation.aggregate(
                eventIds,
                toEventResults(classificationsByEvent),
                ranking.groupBy(),
                ranking.includeBy(),
                ranking.includedCount(),
                ranking.includeReserves());

        return Optional.of(new FetchRankingClassificationDTO(
                toEventColumns(classificationsByEvent),
                toGroupDTOs(groups),
                ranking.groupBy().name()));
    }

    /**
     * An event that has gone missing (deleted after the ranking was saved) drops out of the matrix instead of
     * failing the whole ranking: the configuration is only validated when it is written.
     */
    private Optional<FetchClassificationDTO> classification(String eventId) {
        try {
            return Optional.ofNullable(getEventClassificationServiceCase.getClassification(eventId));
        } catch (DomainException exception) {
            return Optional.empty();
        }
    }

    private static List<FetchRankingClassificationEventDTO> toEventColumns(
            Map<String, FetchClassificationDTO> classificationsByEvent) {
        return classificationsByEvent.values().stream()
                .map(classification -> new FetchRankingClassificationEventDTO(
                        classification.eventId(), classification.eventName(), classification.stageId()))
                .toList();
    }

    private static List<RankingEventResults> toEventResults(
            Map<String, FetchClassificationDTO> classificationsByEvent) {
        List<RankingEventResults> results = new ArrayList<>(classificationsByEvent.size());
        classificationsByEvent.forEach((eventId, classification) -> {
            List<FetchClassificationCompetitorDTO> competitors = classification.obdx() == null
                    ? List.of()
                    : classification.obdx().competitors();
            results.add(new RankingEventResults(eventId, competitors.stream()
                    .map(competitor -> new RankingCompetitorResult(
                            competitor.dogIdentification(),
                            competitor.dogName(),
                            competitor.team(),
                            competitor.country(),
                            competitor.totalScore(),
                            competitor.notCompeting(),
                            Boolean.TRUE.equals(competitor.reserve())))
                    .toList()));
        });
        return results;
    }

    private static List<FetchRankingClassificationGroupDTO> toGroupDTOs(List<RankingGroup> groups) {
        return groups.stream()
                .map(group -> new FetchRankingClassificationGroupDTO(
                        group.id(),
                        group.name(),
                        group.position(),
                        group.tied(),
                        group.total(),
                        group.members().stream()
                                .map(member -> new FetchRankingClassificationMemberDTO(
                                        member.id(),
                                        member.name(),
                                        member.cells().stream()
                                                .map(cell -> new FetchRankingClassificationCellDTO(
                                                        cell.eventId(), cell.score(), cell.counts()))
                                                .toList()))
                                .toList()))
                .toList();
    }
}

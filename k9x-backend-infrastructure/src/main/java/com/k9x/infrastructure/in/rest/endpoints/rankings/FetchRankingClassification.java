package com.k9x.infrastructure.in.rest.endpoints.rankings;

import com.k9x.application.rankings.use_case.GetRankingClassificationServiceCase;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationDTO;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationGroupDTO;
import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.RankingsFetchOneApiDelegate;
import com.k9x.oas.stub.model.RankingClassificationCellResponseDTO;
import com.k9x.oas.stub.model.RankingClassificationEventResponseDTO;
import com.k9x.oas.stub.model.RankingClassificationGroupResponseDTO;
import com.k9x.oas.stub.model.RankingClassificationMemberResponseDTO;
import com.k9x.oas.stub.model.RankingClassificationResponseDTO;
import org.springframework.http.ResponseEntity;

/**
 * Public ranking results. Carries no {@code UserInfoDTO}: the path is outside {@code /secured/}, so the auth
 * filter lets the request through and there is no user on it to inject.
 */
public class FetchRankingClassification implements RankingsFetchOneApiDelegate {

    private final GetRankingClassificationServiceCase getRankingClassificationServiceCase;
    private final ReferenceNameResolver referenceNames;

    public FetchRankingClassification(
            GetRankingClassificationServiceCase getRankingClassificationServiceCase,
            ReferenceNameResolver referenceNames) {
        this.getRankingClassificationServiceCase = getRankingClassificationServiceCase;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<RankingClassificationResponseDTO> fetchRanking(String id) {
        return getRankingClassificationServiceCase.getRankingClassification(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private RankingClassificationResponseDTO toResponse(FetchRankingClassificationDTO ranking) {
        boolean groupedByCountry = RankingGroupBy.COUNTRY.name().equals(ranking.groupBy());
        return new RankingClassificationResponseDTO(
                ranking.events().stream()
                        .map(event -> new RankingClassificationEventResponseDTO(
                                event.id(), event.name(), event.stageId()))
                        .toList(),
                ranking.groups().stream()
                        .map(group -> toGroup(group, groupedByCountry))
                        .toList());
    }

    private RankingClassificationGroupResponseDTO toGroup(FetchRankingClassificationGroupDTO group,
                                                          boolean groupedByCountry) {
        // Grouping by country yields ISO codes, which are translated here just like everywhere else at the
        // REST boundary. Individual and team criteria are already display names.
        String name = groupedByCountry ? referenceNames.countryName(group.id()) : group.name();
        return new RankingClassificationGroupResponseDTO(
                group.id(),
                name,
                group.position(),
                group.tied(),
                group.total(),
                group.members().stream()
                        .map(member -> new RankingClassificationMemberResponseDTO(
                                member.id(),
                                member.name(),
                                member.cells().stream()
                                        .map(cell -> new RankingClassificationCellResponseDTO(
                                                cell.eventId(), cell.score(), cell.counts()))
                                        .toList()))
                        .toList());
    }
}

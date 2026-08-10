package com.k9x.infrastructure.out.enums.rankings;

import com.k9x.application.rankings.port.GetRankingGroupByListPort;
import com.k9x.application.rankings.use_case.dto.RankingCriterionDTO;
import com.k9x.domain.rankings.RankingGroupBy;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.List;

/**
 * Exposes the grouping criteria as a selectable catalogue.
 *
 * <p>Unlike breeds or countries, the enum itself lives in the domain module: a grouping criterion is
 * persisted and validated by business rules. This adapter only walks its values and resolves the label,
 * keeping i18n out of the application layer.
 */
public class RankingGroupByEnumAdapter implements GetRankingGroupByListPort {

    private final MessageSource messageSource;

    public RankingGroupByEnumAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<RankingCriterionDTO> getGroupBys() {
        return Arrays.stream(RankingGroupBy.values())
                .map(groupBy -> new RankingCriterionDTO(groupBy.name(), translate(groupBy)))
                .toList();
    }

    private String translate(RankingGroupBy groupBy) {
        String key = "ranking.group_by." + groupBy.name().toLowerCase() + ".name";
        return messageSource.getMessage(key, null, groupBy.name(), LocaleContextHolder.getLocale());
    }
}

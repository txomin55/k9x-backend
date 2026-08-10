package com.k9x.infrastructure.out.enums.rankings;

import com.k9x.application.rankings.port.GetRankingIncludeByListPort;
import com.k9x.application.rankings.use_case.dto.RankingCriterionDTO;
import com.k9x.domain.rankings.RankingIncludeBy;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.List;

public class RankingIncludeByEnumAdapter implements GetRankingIncludeByListPort {

    private final MessageSource messageSource;

    public RankingIncludeByEnumAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<RankingCriterionDTO> getIncludeBys() {
        return Arrays.stream(RankingIncludeBy.values())
                .map(includeBy -> new RankingCriterionDTO(includeBy.name(), translate(includeBy)))
                .toList();
    }

    private String translate(RankingIncludeBy includeBy) {
        String key = "ranking.include_by." + includeBy.name().toLowerCase() + ".name";
        return messageSource.getMessage(key, null, includeBy.name(), LocaleContextHolder.getLocale());
    }
}

package com.k9x.infrastructure.configuration.enums;

import com.k9x.application.awards.port.GetAwardListPort;
import com.k9x.application.breeds.port.GetBreedListPort;
import com.k9x.application.countries.port.GetCountryListPort;
import com.k9x.application.rankings.port.GetRankingIncludeByListPort;
import com.k9x.application.rankings.port.GetRankingGroupByListPort;
import com.k9x.infrastructure.out.enums.awards.AwardEnumAdapter;
import com.k9x.infrastructure.out.enums.breeds.BreedEnumAdapter;
import com.k9x.infrastructure.out.enums.countries.CountryEnumAdapter;
import com.k9x.infrastructure.out.enums.rankings.RankingIncludeByEnumAdapter;
import com.k9x.infrastructure.out.enums.rankings.RankingGroupByEnumAdapter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnumAdapterConfiguration {

    @Bean
    public GetCountryListPort getCountryListPort(MessageSource messageSource) {
        return new CountryEnumAdapter(messageSource);
    }

    @Bean
    public GetBreedListPort getBreedListPort(MessageSource messageSource) {
        return new BreedEnumAdapter(messageSource);
    }

    @Bean
    public GetAwardListPort getAwardListPort() {
        return new AwardEnumAdapter();
    }

    @Bean
    public GetRankingGroupByListPort getRankingGroupByListPort(MessageSource messageSource) {
        return new RankingGroupByEnumAdapter(messageSource);
    }

    @Bean
    public GetRankingIncludeByListPort getRankingIncludeByListPort(MessageSource messageSource) {
        return new RankingIncludeByEnumAdapter(messageSource);
    }
}

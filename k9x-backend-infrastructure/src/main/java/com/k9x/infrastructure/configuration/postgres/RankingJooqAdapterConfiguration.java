package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.rankings.port.DeleteRankingPersistencePort;
import com.k9x.application.rankings.port.GetActiveEventIdsPersistencePort;
import com.k9x.application.rankings.port.GetRankingDetailPersistencePort;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.rankings.port.SaveRankingPersistencePort;
import com.k9x.infrastructure.out.postgres.rankings.DeleteRankingJooqAdapter;
import com.k9x.infrastructure.out.postgres.rankings.GetActiveEventIdsJooqAdapter;
import com.k9x.infrastructure.out.postgres.rankings.GetRankingDetailJooqAdapter;
import com.k9x.infrastructure.out.postgres.rankings.GetRankingJooqAdapter;
import com.k9x.infrastructure.out.postgres.rankings.SaveRankingJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RankingJooqAdapterConfiguration {

    private final DSLContext dsl;

    RankingJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public SaveRankingPersistencePort saveRankingPersistencePort() {
        return new SaveRankingJooqAdapter(dsl);
    }

    @Bean
    public GetRankingPersistencePort getRankingPersistencePort() {
        return new GetRankingJooqAdapter(dsl);
    }

    @Bean
    public GetRankingDetailPersistencePort getRankingDetailPersistencePort() {
        return new GetRankingDetailJooqAdapter(dsl);
    }

    @Bean
    public DeleteRankingPersistencePort deleteRankingPersistencePort() {
        return new DeleteRankingJooqAdapter(dsl);
    }

    @Bean
    public GetActiveEventIdsPersistencePort getActiveEventIdsPersistencePort() {
        return new GetActiveEventIdsJooqAdapter(dsl);
    }
}

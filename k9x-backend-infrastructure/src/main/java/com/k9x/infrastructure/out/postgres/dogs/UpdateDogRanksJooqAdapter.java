package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.UpdateDogRanksPersistencePort;
import com.k9x.application.dogs.rank.port.payload.DogRankUpdatePayload;
import org.jooq.DSLContext;
import org.jooq.Query;

import java.util.List;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.DOGS;

public class UpdateDogRanksJooqAdapter implements UpdateDogRanksPersistencePort {

    private final DSLContext dsl;

    public UpdateDogRanksJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateRanks(List<DogRankUpdatePayload> ranks) {
        List<? extends Query> batch = ranks.stream()
                .map(r -> dsl.update(DOGS)
                        .set(DOGS.RANK, r.rank())
                        .set(DOGS.LAST_UPDATE, r.lastUpdate())
                        .where(DOGS.ID.eq(r.dogId())))
                .toList();
        if (!batch.isEmpty()) {
            dsl.batch(batch).execute();
        }
    }
}

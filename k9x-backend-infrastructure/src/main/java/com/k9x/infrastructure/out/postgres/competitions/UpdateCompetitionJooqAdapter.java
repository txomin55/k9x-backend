package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.UpdateCompetitionPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateCompetitionJooqAdapter implements UpdateCompetitionPersistencePort {

    private final DSLContext dsl;

    public UpdateCompetitionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateCompetition(String id, String name, String description, String address,
                                  Double coordAlt, Double coordLong, long lastUpdate) {
        dsl.update(Tables.COMPETITIONS)
                .set(Tables.COMPETITIONS.NAME, name)
                .set(Tables.COMPETITIONS.DESCRIPTION, description)
                .set(Tables.COMPETITIONS.ADDRESS, address)
                .set(Tables.COMPETITIONS.COORD_ALT, coordAlt)
                .set(Tables.COMPETITIONS.COORD_LONG, coordLong)
                .set(Tables.COMPETITIONS.LAST_UPDATE, lastUpdate)
                .where(Tables.COMPETITIONS.ID.eq(id))
                .execute();
    }
}

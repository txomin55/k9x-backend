package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.payload.UpdateCompetitionPersistencePayload;
import com.k9x.application.competitions.port.UpdateCompetitionPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateCompetitionJooqAdapter implements UpdateCompetitionPersistencePort {

    private final DSLContext dsl;

    public UpdateCompetitionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateCompetition(String id, UpdateCompetitionPersistencePayload payload) {
        dsl.update(Tables.COMPETITIONS)
                .set(Tables.COMPETITIONS.NAME, payload.name())
                .set(Tables.COMPETITIONS.DESCRIPTION, payload.description())
                .set(Tables.COMPETITIONS.COUNTRY, payload.country())
                .set(Tables.COMPETITIONS.ADDRESS, payload.address())
                .set(Tables.COMPETITIONS.COORD_ALT, payload.coordAlt())
                .set(Tables.COMPETITIONS.COORD_LONG, payload.coordLong())
                .set(Tables.COMPETITIONS.LAST_UPDATE, payload.lastUpdate())
                .where(Tables.COMPETITIONS.ID.eq(id))
                .execute();
    }
}

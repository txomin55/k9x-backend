package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.payload.UpdateDogPersistencePayload;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateDogJooqAdapter implements UpdateDogPersistencePort {

    private final DSLContext dsl;

    public UpdateDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateDog(String identification, UpdateDogPersistencePayload payload) {
        dsl.update(Tables.DOGS)
                .set(Tables.DOGS.NAME, payload.name())
                .set(Tables.DOGS.IMAGE, payload.image())
                .set(Tables.DOGS.BREED, payload.breed())
                .set(Tables.DOGS.ORIGIN, payload.origin())
                .set(Tables.DOGS.OWNER, payload.owner())
                .set(Tables.DOGS.HANDLER, payload.handler())
                .set(Tables.DOGS.TEAM, payload.team())
                .set(Tables.DOGS.COUNTRY, payload.country())
                .set(Tables.DOGS.SEX, payload.sex() == null ? null : payload.sex().name())
                .set(Tables.DOGS.WITHERS_CM, payload.withersCm())
                .set(Tables.DOGS.THREE_FCI_GENERATIONS_CONFIRMED, payload.threeFciGenerationsConfirmed())
                .set(Tables.DOGS.LAST_UPDATE, payload.lastUpdate())
                .where(Tables.DOGS.IDENTIFICATION.eq(identification))
                .execute();
    }
}

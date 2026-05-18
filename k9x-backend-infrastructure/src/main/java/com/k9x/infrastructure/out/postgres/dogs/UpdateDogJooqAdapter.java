package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.payload.UpdateDogPersistencePayload;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateDogJooqAdapter implements UpdateDogPersistencePort {

    private final DSLContext dsl;

    public UpdateDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateDog(String id, UpdateDogPersistencePayload payload) {
        dsl.update(Tables.DOGS)
                .set(Tables.DOGS.NAME, payload.name())
                .set(Tables.DOGS.IMAGE, payload.image())
                .set(Tables.DOGS.BREED, payload.breed())
                .set(Tables.DOGS.IDENTITY, payload.identity())
                .set(Tables.DOGS.OWNER, payload.owner())
                .set(Tables.DOGS.TEAM, payload.team())
                .set(Tables.DOGS.COUNTRY, payload.country())
                .set(Tables.DOGS.LAST_UPDATE, payload.lastUpdate())
                .where(Tables.DOGS.ID.eq(id))
                .execute();
    }
}

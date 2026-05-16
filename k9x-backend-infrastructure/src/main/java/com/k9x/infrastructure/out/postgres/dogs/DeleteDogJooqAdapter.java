package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class DeleteDogJooqAdapter implements DeleteDogPersistencePort {

    private final DSLContext dsl;

    public DeleteDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void deleteDog(String id, long deletedAt) {
        dsl.update(Tables.DOGS)
                .set(Tables.DOGS.DELETED_AT, deletedAt)
                .where(Tables.DOGS.ID.eq(id))
                .execute();
    }
}

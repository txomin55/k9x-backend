package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateDogJooqAdapter implements CreateDogPersistencePort {

    private final DSLContext dsl;

    public CreateDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Upsert on the chip (id) primary key: a brand-new chip is inserted, while a chip that already belongs to a
     * soft-deleted dog reactivates that row (deleted_at = null) with the new data. Active chip/identity
     * collisions are rejected in the service case, so the conflict branch only ever hits deleted rows.
     */
    @Override
    public void createDog(String id, String name, String image, String breed, String identity,
                          String owner, String handler, String creator, String team, String country,
                          Sex sex, Integer withersCm, Boolean threeFciGenerationsConfirmed, long createdAt) {
        String sexName = sex == null ? null : sex.name();
        dsl.insertInto(Tables.DOGS)
                .set(Tables.DOGS.ID, id)
                .set(Tables.DOGS.NAME, name)
                .set(Tables.DOGS.IMAGE, image)
                .set(Tables.DOGS.BREED, breed)
                .set(Tables.DOGS.IDENTITY, identity)
                .set(Tables.DOGS.OWNER, owner)
                .set(Tables.DOGS.HANDLER, handler)
                .set(Tables.DOGS.CREATOR, creator)
                .set(Tables.DOGS.TEAM, team)
                .set(Tables.DOGS.COUNTRY, country)
                .set(Tables.DOGS.SEX, sexName)
                .set(Tables.DOGS.WITHERS_CM, withersCm)
                .set(Tables.DOGS.THREE_FCI_GENERATIONS_CONFIRMED, threeFciGenerationsConfirmed)
                .set(Tables.DOGS.CREATED_AT, createdAt)
                .set(Tables.DOGS.LAST_UPDATE, createdAt)
                .onConflict(Tables.DOGS.ID)
                .doUpdate()
                .set(Tables.DOGS.NAME, name)
                .set(Tables.DOGS.IMAGE, image)
                .set(Tables.DOGS.BREED, breed)
                .set(Tables.DOGS.IDENTITY, identity)
                .set(Tables.DOGS.OWNER, owner)
                .set(Tables.DOGS.HANDLER, handler)
                .set(Tables.DOGS.CREATOR, creator)
                .set(Tables.DOGS.TEAM, team)
                .set(Tables.DOGS.COUNTRY, country)
                .set(Tables.DOGS.SEX, sexName)
                .set(Tables.DOGS.WITHERS_CM, withersCm)
                .set(Tables.DOGS.THREE_FCI_GENERATIONS_CONFIRMED, threeFciGenerationsConfirmed)
                .set(Tables.DOGS.CREATED_AT, createdAt)
                .set(Tables.DOGS.LAST_UPDATE, createdAt)
                .set(Tables.DOGS.DELETED_AT, (Long) null)
                .execute();
    }
}

package com.k9x.infrastructure.out.postgres.dog.adapter;

import com.k9x.application.dog.port.GetDogPersistencePort;
import com.k9x.domain.commons.entitystatemachine.EntityStateMachine;
import com.k9x.domain.dog.model.Dog;
import com.k9x.infrastructure.out.postgres.jooq.generated.Tables;
import org.jooq.DSLContext;

public class GetDogJooqAdapter implements GetDogPersistencePort {

    private final DSLContext dsl;

    public GetDogJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Dog getDog(String id) {
        var record = dsl.select()
                .from(Tables.DOGS)
                .where(Tables.DOGS.ID.eq(id))
                .fetchOne();
        if (record == null) {
            return new Dog();
        }
        return new Dog(
                record.get(Tables.DOGS.ID),
                record.get(Tables.DOGS.NAME),
                record.get(Tables.DOGS.IMAGE),
                record.get(Tables.DOGS.OWNER),
                EntityStateMachine.valueOfState(record.get(Tables.DOGS.STATE)),
                record.get(Tables.DOGS.LAST_UPDATE)
        );
    }
}

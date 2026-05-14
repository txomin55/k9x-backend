package com.k9x.infrastructure.out.postgres.dog.adapter;

import com.k9x.application.dog.port.GetDogListPersistencePort;
import com.k9x.domain.commons.entitystatemachine.EntityStateMachine;
import com.k9x.domain.dog.model.Dog;
import com.k9x.infrastructure.out.postgres.jooq.generated.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetDogListJooqAdapter implements GetDogListPersistencePort {

    private final DSLContext dsl;

    public GetDogListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Dog> getDogs(String owner) {
        return dsl.select()
                .from(Tables.DOGS)
                .where(Tables.DOGS.OWNER.eq(owner))
                .fetch(r -> new Dog(
                        r.get(Tables.DOGS.ID),
                        r.get(Tables.DOGS.NAME),
                        r.get(Tables.DOGS.IMAGE),
                        r.get(Tables.DOGS.OWNER),
                        EntityStateMachine.valueOfState(r.get(Tables.DOGS.STATE)),
                        r.get(Tables.DOGS.LAST_UPDATE)
                ));
    }
}

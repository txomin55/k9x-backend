package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.CreateUserPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateUserJooqAdapter implements CreateUserPersistencePort {

    private final DSLContext dsl;

    public CreateUserJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void createUser(String email, String image) {
        dsl.insertInto(Tables.USERS)
                .set(Tables.USERS.ID, email)
                .set(Tables.USERS.EMAIL, email)
                .set(Tables.USERS.IMAGE, image)
                .execute();
    }
}

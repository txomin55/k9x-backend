package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class GetUserInfoJooqAdapter implements GetUserInfoPersistencePort {

    private final DSLContext dsl;

    public GetUserInfoJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public UserInfoDTO findById(String id) {
        return dsl.select(
                        Tables.USERS.ID,
                        Tables.USERS.EMAIL,
                        Tables.ORGANIZERS.USER_ID
                )
                .from(Tables.USERS)
                .leftJoin(Tables.ORGANIZERS).on(Tables.ORGANIZERS.USER_ID.eq(Tables.USERS.ID))
                .where(Tables.USERS.ID.eq(id))
                .fetchOptional(r -> new UserInfoDTO(
                        r.get(Tables.USERS.ID),
                        r.get(Tables.USERS.EMAIL),
                        r.get(Tables.ORGANIZERS.USER_ID) != null
                )).orElse(null);
    }
}

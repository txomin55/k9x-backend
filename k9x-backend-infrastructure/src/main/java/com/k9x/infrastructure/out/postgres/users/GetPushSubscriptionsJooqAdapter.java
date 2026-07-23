package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.port.GetPushSubscriptionsPersistencePort;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetPushSubscriptionsJooqAdapter implements GetPushSubscriptionsPersistencePort {

    private final DSLContext dsl;

    public GetPushSubscriptionsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<PushSubscriptionTargetDTO> getByUserId(String userId) {
        return dsl.select(Tables.PUSH_SUBSCRIPTIONS.ENDPOINT, Tables.PUSH_SUBSCRIPTIONS.P256DH, Tables.PUSH_SUBSCRIPTIONS.AUTH)
                .from(Tables.PUSH_SUBSCRIPTIONS)
                .where(Tables.PUSH_SUBSCRIPTIONS.USER_ID.eq(userId))
                .fetch(r -> new PushSubscriptionTargetDTO(
                        r.get(Tables.PUSH_SUBSCRIPTIONS.ENDPOINT),
                        r.get(Tables.PUSH_SUBSCRIPTIONS.P256DH),
                        r.get(Tables.PUSH_SUBSCRIPTIONS.AUTH)));
    }
}

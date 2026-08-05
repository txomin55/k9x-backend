package com.k9x.infrastructure.out.postgres.subscriptions;

import com.k9x.application.subscriptions.port.GetUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.use_case.dto.UserSubscriptionsDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GetUserSubscriptionsJooqAdapter implements GetUserSubscriptionsPersistencePort {

    private final DSLContext dsl;

    public GetUserSubscriptionsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public UserSubscriptionsDTO getUserSubscriptions(String userId) {
        return dsl.select(Tables.USER_SUBSCRIPTIONS.EVENT_IDS)
                .from(Tables.USER_SUBSCRIPTIONS)
                .where(Tables.USER_SUBSCRIPTIONS.USER_ID.eq(userId))
                .fetchOptional(r -> new UserSubscriptionsDTO(toList(r.get(Tables.USER_SUBSCRIPTIONS.EVENT_IDS))))
                .orElseGet(UserSubscriptionsDTO::empty);
    }

    private static List<String> toList(String[] ids) {
        return ids == null ? List.of() : Arrays.stream(ids).filter(Objects::nonNull).toList();
    }
}

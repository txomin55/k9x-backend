package com.k9x.infrastructure.out.postgres.subscriptions;

import com.k9x.application.subscriptions.port.UpdateUserSubscriptionPersistencePort;
import com.k9x.application.subscriptions.port.payload.UpdateUserSubscriptionPersistencePayload;
import com.k9x.domain.subscriptions.SubscriptionKind;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import java.util.List;

/**
 * Adds or removes a set of ids from the list of the kind being toggled, in a single statement. Both
 * directions are idempotent: every id is first removed, and the subscribe branch then appends the whole
 * set back, so an id can never be stored twice and removing an absent id is a no-op. This is where each
 * {@link SubscriptionKind} is mapped to its column, so a new kind only needs a new column plus a case here.
 */
public class UpdateUserSubscriptionJooqAdapter implements UpdateUserSubscriptionPersistencePort {

    private final DSLContext dsl;

    public UpdateUserSubscriptionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateUserSubscription(UpdateUserSubscriptionPersistencePayload payload) {
        TableField<?, String[]> subscriptions = columnFor(payload.kind());
        List<String> targetIds = payload.targetIds();

        Field<String[]> updated = withoutTargetIds(subscriptions, targetIds);
        if (payload.subscribe()) {
            updated = DSL.field("array_cat({0}, {1})", String[].class, updated,
                    DSL.val(targetIds.toArray(String[]::new), subscriptions.getDataType()));
        }

        dsl.update(Tables.USER_SUBSCRIPTIONS)
                .set(subscriptions, updated)
                .where(Tables.USER_SUBSCRIPTIONS.USER_ID.eq(payload.userId()))
                .execute();
    }

    private static Field<String[]> withoutTargetIds(TableField<?, String[]> subscriptions, List<String> targetIds) {
        Field<String[]> pruned = subscriptions;
        for (String targetId : targetIds) {
            pruned = DSL.field("array_remove({0}, {1})", String[].class, pruned, DSL.val(targetId));
        }
        return pruned;
    }

    private static TableField<?, String[]> columnFor(SubscriptionKind kind) {
        return switch (kind) {
            case EVENT -> Tables.USER_SUBSCRIPTIONS.EVENT_IDS;
        };
    }
}

package com.k9x.application.notifications.port;

import com.k9x.application.notifications.valueobjects.PushDeliveryStatus;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;

public interface SendPushNotificationPort {

    /**
     * Delivers a notification to a single push subscription. Implementations must never throw: any
     * transport or server failure is reported as {@link PushDeliveryStatus#FAILED} so one bad
     * subscription cannot abort delivery to the rest.
     */
    PushDeliveryStatus send(PushSubscriptionTargetDTO target, PushNotification notification);
}

package com.k9x.infrastructure.out.push;

import com.k9x.application.notifications.port.SendPushNotificationPort;
import com.k9x.application.notifications.valueobjects.PushDeliveryStatus;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * Fallback used when no VAPID key pair is configured (e.g. local development). Reports every attempt as
 * {@link PushDeliveryStatus#FAILED} so nothing is sent and no subscription is ever pruned as expired.
 */
public class NoOpPushNotificationAdapter implements SendPushNotificationPort {

    private static final Logger log = System.getLogger(NoOpPushNotificationAdapter.class.getName());

    @Override
    public PushDeliveryStatus send(PushSubscriptionTargetDTO target, PushNotification notification) {
        log.log(Level.WARNING, "Push NOT sent: no VAPID keys configured (set VAPID_PUBLIC_KEY / VAPID_PRIVATE_KEY)");
        return PushDeliveryStatus.FAILED;
    }
}

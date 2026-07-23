package com.k9x.infrastructure.out.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.port.SendPushNotificationPort;
import com.k9x.application.notifications.valueobjects.PushDeliveryStatus;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Map;

/**
 * Sends Web Push (VAPID) notifications via the {@code web-push} library. The VAPID key pair identifies
 * this server to the push service; the payload is delivered encrypted end-to-end using the
 * subscription's own {@code p256dh}/{@code auth} keys, so it is opaque to the push service.
 *
 * <p>Never throws: a 404/410 Gone (subscription no longer valid) maps to {@link PushDeliveryStatus#EXPIRED}
 * and any other error to {@link PushDeliveryStatus#FAILED}, per the port contract.
 */
public class WebPushNotificationAdapter implements SendPushNotificationPort {

    private static final Logger log = System.getLogger(WebPushNotificationAdapter.class.getName());

    private static final int NOT_FOUND = 404;
    private static final int GONE = 410;

    private final PushService pushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebPushNotificationAdapter(String publicKey, String privateKey, String subject) throws GeneralSecurityException {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    @Override
    public PushDeliveryStatus send(PushSubscriptionTargetDTO target, PushNotification notification) {
        try {
            Subscription subscription =
                    new Subscription(target.endpoint(), new Subscription.Keys(target.p256dh(), target.auth()));
            HttpResponse response = pushService.send(new Notification(subscription, serialize(notification)));
            int status = response.getStatusLine().getStatusCode();
            log.log(Level.INFO, "Web push to {0} returned HTTP {1}", target.endpoint(), status);
            if (status == NOT_FOUND || status == GONE) {
                return PushDeliveryStatus.EXPIRED;
            }
            if (status >= 200 && status < 300) {
                return PushDeliveryStatus.DELIVERED;
            }
            log.log(Level.WARNING, "Web push rejected with HTTP {0} for {1}", status, target.endpoint());
            return PushDeliveryStatus.FAILED;
        } catch (Exception e) {
            log.log(Level.ERROR, "Web push failed for " + target.endpoint(), e);
            return PushDeliveryStatus.FAILED;
        }
    }

    private String serialize(PushNotification notification) throws Exception {
        return objectMapper.writeValueAsString(
                Map.of("type", notification.type().name(), "metadata", notification.metadata()));
    }
}

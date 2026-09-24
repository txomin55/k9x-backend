package com.k9x.infrastructure.out.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.port.SendPushNotificationPort;
import com.k9x.application.notifications.valueobjects.PushDeliveryStatus;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;
import com.k9x.application.utils.date.DateUtils;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Map;

/**
 * Sends Web Push (VAPID) notifications with the JDK alone: {@link Vapid} identifies this server to the push
 * service, {@link WebPushEncryption} encrypts the payload end-to-end with the subscription's own
 * {@code p256dh}/{@code auth} keys, so it is opaque to the push service, and {@link HttpClient} posts it. It
 * replaced the {@code web-push} library, which pulled BouncyCastle, Netty and two HTTP clients into a 512 MB box.
 *
 * <p>Never throws: a 404/410 Gone (subscription no longer valid) maps to {@link PushDeliveryStatus#EXPIRED}
 * and any other error to {@link PushDeliveryStatus#FAILED}, per the port contract.
 */
public class WebPushNotificationAdapter implements SendPushNotificationPort {

    private static final Logger log = System.getLogger(WebPushNotificationAdapter.class.getName());

    private static final int NOT_FOUND = 404;
    private static final int GONE = 410;
    /** How long the push service keeps an undelivered message: 28 days, the web-push libraries' default. */
    static final String TIME_TO_LIVE_SECONDS = "2419200";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final Vapid vapid;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebPushNotificationAdapter(String publicKey, String privateKey, String subject) throws GeneralSecurityException {
        this.vapid = new Vapid(publicKey, privateKey, subject, objectMapper);
    }

    @Override
    public PushDeliveryStatus send(PushSubscriptionTargetDTO target, PushNotification notification) {
        try {
            byte[] body = WebPushEncryption.encrypt(serialize(notification),
                    P256.decodeBase64(target.p256dh()), P256.decodeBase64(target.auth()));
            HttpRequest request = HttpRequest.newBuilder(URI.create(target.endpoint()))
                    .timeout(TIMEOUT)
                    .header("TTL", TIME_TO_LIVE_SECONDS)
                    .header("Content-Encoding", "aes128gcm")
                    .header("Content-Type", "application/octet-stream")
                    .header("Authorization", vapid.authorization(target.endpoint(), DateUtils.nowUtcMillis() / 1000))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
            int status = httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            log.log(Level.INFO, "Web push to {0} returned HTTP {1}", target.endpoint(), status);
            if (status == NOT_FOUND || status == GONE) {
                return PushDeliveryStatus.EXPIRED;
            }
            if (status >= 200 && status < 300) {
                return PushDeliveryStatus.DELIVERED;
            }
            log.log(Level.WARNING, "Web push rejected with HTTP {0} for {1}", status, target.endpoint());
            return PushDeliveryStatus.FAILED;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.ERROR, "Web push interrupted for " + target.endpoint(), e);
            return PushDeliveryStatus.FAILED;
        } catch (Exception e) {
            log.log(Level.ERROR, "Web push failed for " + target.endpoint(), e);
            return PushDeliveryStatus.FAILED;
        }
    }

    private byte[] serialize(PushNotification notification) throws Exception {
        return objectMapper.writeValueAsBytes(
                Map.of("type", notification.type().name(), "metadata", notification.metadata()));
    }
}

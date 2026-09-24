package com.k9x.infrastructure.out.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.valueobjects.PushDeliveryStatus;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class WebPushNotificationAdapterTest {

    private final AtomicInteger status = new AtomicInteger(201);
    private final AtomicReference<Headers> headers = new AtomicReference<>();
    private final AtomicReference<byte[]> body = new AtomicReference<>();
    private HttpServer pushService;
    private KeyPair subscriber;
    private byte[] auth;
    private WebPushNotificationAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        pushService = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        pushService.createContext("/push", exchange -> {
            headers.set(exchange.getRequestHeaders());
            body.set(exchange.getRequestBody().readAllBytes());
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        pushService.start();

        KeyPair vapid = P256.generateKeyPair();
        adapter = new WebPushNotificationAdapter(VapidTest.publicKey(vapid), VapidTest.privateKey(vapid),
                "mailto:k9x.support@gmail.com");
        subscriber = P256.generateKeyPair();
        auth = new byte[16];
        new SecureRandom().nextBytes(auth);
    }

    @AfterEach
    void tearDown() {
        pushService.stop(0);
    }

    private PushSubscriptionTargetDTO target(String endpoint) {
        return new PushSubscriptionTargetDTO(endpoint,
                P256.encodeBase64(P256.encode((ECPublicKey) subscriber.getPublic())), P256.encodeBase64(auth));
    }

    private String endpoint() {
        return "http://127.0.0.1:" + pushService.getAddress().getPort() + "/push/subscription-1";
    }

    private static PushNotification notification() {
        return new PushNotification(com.k9x.application.notifications.valueobjects.NotificationType.values()[0],
                java.util.Map.of("eventId", "evt-1"));
    }

    @Test
    void posts_an_encrypted_vapid_signed_message_the_subscriber_can_read() throws Exception {
        assertThat(adapter.send(target(endpoint()), notification())).isEqualTo(PushDeliveryStatus.DELIVERED);

        assertThat(headers.get().getFirst("Content-Encoding")).isEqualTo("aes128gcm");
        assertThat(headers.get().getFirst("TTL")).isEqualTo(WebPushNotificationAdapter.TIME_TO_LIVE_SECONDS);
        assertThat(headers.get().getFirst("Authorization")).startsWith("vapid t=").contains(", k=");
        JsonNode payload = new ObjectMapper().readTree(WebPushEncryptionTest.decrypt(body.get(), subscriber, auth));
        assertThat(payload.get("type").asText()).isEqualTo(notification().type().name());
        assertThat(payload.get("metadata").get("eventId").asText()).isEqualTo("evt-1");
    }

    @Test
    void a_gone_or_unknown_subscription_is_expired() {
        status.set(410);
        assertThat(adapter.send(target(endpoint()), notification())).isEqualTo(PushDeliveryStatus.EXPIRED);
        status.set(404);
        assertThat(adapter.send(target(endpoint()), notification())).isEqualTo(PushDeliveryStatus.EXPIRED);
    }

    @Test
    void any_other_rejection_or_an_unreachable_service_fails_without_throwing() {
        status.set(500);
        assertThat(adapter.send(target(endpoint()), notification())).isEqualTo(PushDeliveryStatus.FAILED);

        pushService.stop(0);
        assertThat(adapter.send(target(endpoint()), notification())).isEqualTo(PushDeliveryStatus.FAILED);
    }
}

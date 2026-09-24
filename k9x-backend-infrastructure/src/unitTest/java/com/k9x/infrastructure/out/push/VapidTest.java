package com.k9x.infrastructure.out.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VapidTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    static String publicKey(KeyPair keys) {
        return P256.encodeBase64(P256.encode((ECPublicKey) keys.getPublic()));
    }

    static String privateKey(KeyPair keys) {
        return P256.encodeBase64(P256.encode((ECPrivateKey) keys.getPrivate()));
    }

    @Test
    void signs_an_es256_token_for_the_push_service_origin_that_verifies_with_the_public_key() throws Exception {
        KeyPair keys = P256.generateKeyPair();
        Vapid vapid = new Vapid(publicKey(keys), privateKey(keys), "mailto:k9x.support@gmail.com", objectMapper);

        String header = vapid.authorization("https://fcm.googleapis.com/fcm/send/abc123", 1_000L);

        assertThat(header).startsWith("vapid t=").endsWith(", k=" + publicKey(keys));
        String[] jwt = header.substring("vapid t=".length(), header.indexOf(", k=")).split("\\.");
        assertThat(jwt).hasSize(3);
        JsonNode jose = objectMapper.readTree(P256.decodeBase64(jwt[0]));
        JsonNode claims = objectMapper.readTree(P256.decodeBase64(jwt[1]));
        assertThat(jose.get("alg").asText()).isEqualTo("ES256");
        assertThat(claims.get("aud").asText()).isEqualTo("https://fcm.googleapis.com");
        assertThat(claims.get("exp").asLong()).isEqualTo(1_000L + Vapid.TOKEN_LIFETIME_SECONDS);
        assertThat(claims.get("sub").asText()).isEqualTo("mailto:k9x.support@gmail.com");

        byte[] signature = P256.decodeBase64(jwt[2]);
        assertThat(signature).hasSize(64); // JWS ES256 is R || S, not DER
        Signature verifier = Signature.getInstance(Vapid.SIGNATURE_ALGORITHM);
        verifier.initVerify(keys.getPublic());
        verifier.update((jwt[0] + "." + jwt[1]).getBytes(StandardCharsets.US_ASCII));
        assertThat(verifier.verify(signature)).isTrue();
    }

    @Test
    void keeps_a_non_default_port_in_the_audience() {
        assertThat(Vapid.audience("http://localhost:8080/push/1")).isEqualTo("http://localhost:8080");
        assertThat(Vapid.audience("https://updates.push.services.mozilla.com/wpush/v2/x"))
                .isEqualTo("https://updates.push.services.mozilla.com");
    }

    @Test
    void refuses_a_public_key_that_is_not_the_private_key_pair() throws Exception {
        KeyPair one = P256.generateKeyPair();
        KeyPair other = P256.generateKeyPair();

        assertThatThrownBy(() -> new Vapid(publicKey(one), privateKey(other), "mailto:a@b.c", objectMapper))
                .isInstanceOf(GeneralSecurityException.class)
                .hasMessageContaining("does not match");
    }
}

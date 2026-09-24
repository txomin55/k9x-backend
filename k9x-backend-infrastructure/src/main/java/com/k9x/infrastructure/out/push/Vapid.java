package com.k9x.infrastructure.out.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Identifies this server to push services (VAPID, RFC 8292): a JWT signed with ES256 by the VAPID private key,
 * sent with the public key in the {@code Authorization} header. The JDK signs ES256 directly in the JWS form
 * ({@code R || S}) with {@code SHA256withECDSAinP1363Format}.
 */
final class Vapid {

    /** Push services reject tokens valid for more than 24 h; 12 h is what the web-push libraries use. */
    static final long TOKEN_LIFETIME_SECONDS = 12 * 60 * 60;
    static final String SIGNATURE_ALGORITHM = "SHA256withECDSAinP1363Format";

    private final ECPrivateKey privateKey;
    private final byte[] publicKey;
    private final String subject;
    private final ObjectMapper objectMapper;

    /**
     * Fails fast when the two keys are not a pair: a signature made with the private key must verify with the
     * public one, otherwise every push service would reject every message with a 403.
     */
    Vapid(String publicKey, String privateKey, String subject, ObjectMapper objectMapper) throws GeneralSecurityException {
        this.publicKey = P256.decodeBase64(publicKey);
        this.privateKey = P256.privateKey(P256.decodeBase64(privateKey));
        this.subject = subject;
        this.objectMapper = objectMapper;
        assertPair(P256.publicKey(this.publicKey), this.privateKey);
    }

    /** The {@code Authorization} header value for a message to {@code endpoint}. */
    String authorization(String endpoint, long nowEpochSeconds) throws GeneralSecurityException {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("aud", audience(endpoint));
        claims.put("exp", nowEpochSeconds + TOKEN_LIFETIME_SECONDS);
        claims.put("sub", subject);
        String signingInput = base64Json(Map.of("typ", "JWT", "alg", "ES256")) + "." + base64Json(claims);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        String token = signingInput + "." + P256.encodeBase64(signature.sign());
        return "vapid t=" + token + ", k=" + P256.encodeBase64(publicKey);
    }

    /** The push service's origin: the token is only valid for the service it is sent to. */
    static String audience(String endpoint) {
        URI uri = URI.create(endpoint);
        return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
    }

    private String base64Json(Map<String, Object> value) throws GeneralSecurityException {
        try {
            return P256.encodeBase64(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
            throw new GeneralSecurityException("Unable to serialize the VAPID token", e);
        }
    }

    private static void assertPair(ECPublicKey publicKey, ECPrivateKey privateKey) throws GeneralSecurityException {
        byte[] probe = "k9x-vapid-key-check".getBytes(StandardCharsets.US_ASCII);
        Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
        signer.initSign(privateKey);
        signer.update(probe);
        Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
        verifier.initVerify(publicKey);
        verifier.update(probe);
        if (!verifier.verify(signer.sign())) {
            throw new GeneralSecurityException("The VAPID public key does not match the private key");
        }
    }
}

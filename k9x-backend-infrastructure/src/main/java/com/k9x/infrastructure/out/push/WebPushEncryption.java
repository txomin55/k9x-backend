package com.k9x.infrastructure.out.push;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

/**
 * Message encryption for Web Push (RFC 8291) in the {@code aes128gcm} content coding (RFC 8188), with the JDK's
 * own crypto: an ephemeral P-256 key agreed (ECDH) with the subscription's {@code p256dh} key, HKDF-SHA256 mixing
 * in its {@code auth} secret, and AES-128-GCM over a single record. Only the browser that subscribed can read it;
 * the push service relays opaque bytes.
 */
final class WebPushEncryption {

    /** The record size advertised in the header; a push message fits in one record. */
    static final int RECORD_SIZE = 4096;
    private static final int SALT_BYTES = 16;
    private static final int TAG_BYTES = 16;
    /** Marks the last (and only) record, RFC 8188 section 2. */
    private static final byte LAST_RECORD_DELIMITER = 0x02;
    private static final byte[] KEY_INFO = "WebPush: info\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CEK_INFO = "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] NONCE_INFO = "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII);

    private static final SecureRandom RANDOM = new SecureRandom();

    private WebPushEncryption() {
    }

    /** The request body for {@code payload}, with a fresh ephemeral key and salt as every message must have. */
    static byte[] encrypt(byte[] payload, byte[] subscriberPublicKey, byte[] authSecret) throws GeneralSecurityException {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return encrypt(payload, subscriberPublicKey, authSecret, P256.generateKeyPair(), salt);
    }

    /** Deterministic form, so the RFC 8291 test vector can be replayed. */
    static byte[] encrypt(byte[] payload, byte[] subscriberPublicKey, byte[] authSecret, KeyPair serverKeys,
                          byte[] salt) throws GeneralSecurityException {
        if (payload.length + 1 + TAG_BYTES > RECORD_SIZE) {
            throw new GeneralSecurityException("Push payload of " + payload.length + " bytes does not fit one record");
        }
        byte[] serverPublicKey = P256.encode((ECPublicKey) serverKeys.getPublic());

        KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
        agreement.init(serverKeys.getPrivate());
        agreement.doPhase(P256.publicKey(subscriberPublicKey), true);
        byte[] sharedSecret = agreement.generateSecret();

        byte[] keyMaterial = hkdf(authSecret, sharedSecret, concat(KEY_INFO, subscriberPublicKey, serverPublicKey), 32);
        byte[] contentKey = hkdf(salt, keyMaterial, CEK_INFO, 16);
        byte[] nonce = hkdf(salt, keyMaterial, NONCE_INFO, 12);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(contentKey, "AES"), new GCMParameterSpec(TAG_BYTES * 8, nonce));
        byte[] record = cipher.doFinal(concat(payload, new byte[]{LAST_RECORD_DELIMITER}));

        // Header: salt, record size, and the server's public key as the key id (RFC 8188 section 2.1).
        ByteBuffer body = ByteBuffer.allocate(SALT_BYTES + 4 + 1 + serverPublicKey.length + record.length);
        body.put(salt).putInt(RECORD_SIZE).put((byte) serverPublicKey.length).put(serverPublicKey).put(record);
        return body.array();
    }

    /** HKDF-SHA256 (RFC 5869) for outputs of at most one block, which is all Web Push needs. */
    static byte[] hkdf(byte[] salt, byte[] inputKeyMaterial, byte[] info, int length) throws GeneralSecurityException {
        byte[] pseudoRandomKey = hmac(salt, inputKeyMaterial);
        return Arrays.copyOf(hmac(pseudoRandomKey, concat(info, new byte[]{0x01})), length);
    }

    private static byte[] hmac(byte[] key, byte[] data) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }
}

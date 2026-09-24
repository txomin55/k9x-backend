package com.k9x.infrastructure.out.push;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

import static com.k9x.infrastructure.out.push.P256.decodeBase64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebPushEncryptionTest {

    /** RFC 8291 Appendix A: every input fixed, so the whole message is known in advance. */
    private static final String PLAINTEXT = "When I grow up, I want to be a watermelon";
    private static final String SERVER_PRIVATE = "yfWPiYE-n46HLnH0KqZOF1fJJU3MYrct3AELtAQ-oRw";
    private static final String SERVER_PUBLIC =
            "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8";
    private static final String SUBSCRIBER_PRIVATE = "q1dXpw3UpT5VOmu_cf_v6ih07Aems3njxI-JWgLcM94";
    private static final String SUBSCRIBER_PUBLIC =
            "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
    private static final String SALT = "DGv6ra1nlYgDCS1FRnbzlw";
    private static final String AUTH_SECRET = "BTBZMqHH6r4Tts7J_aSIgg";
    private static final String MESSAGE = "DGv6ra1nlYgDCS1FRnbzlwAAEABBBP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6"
            + "e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A_yl95bQpu6cVPTpK4Mqgkf1CXztLVBSt2Ks3oZwbuwXPXLWyouBWLVWGNWQexSgSx"
            + "sj_Qulcy4a-fN";

    @Test
    void reproduces_the_rfc_8291_test_vector_byte_for_byte() throws Exception {
        KeyPair serverKeys = new KeyPair(P256.publicKey(decodeBase64(SERVER_PUBLIC)),
                P256.privateKey(decodeBase64(SERVER_PRIVATE)));

        byte[] message = WebPushEncryption.encrypt(PLAINTEXT.getBytes(StandardCharsets.UTF_8),
                decodeBase64(SUBSCRIBER_PUBLIC), decodeBase64(AUTH_SECRET), serverKeys, decodeBase64(SALT));

        assertThat(P256.encodeBase64(message)).isEqualTo(MESSAGE);
    }

    @Test
    void a_fresh_message_decrypts_back_with_the_subscriber_keys() throws Exception {
        KeyPair subscriber = P256.generateKeyPair();
        byte[] auth = new byte[16];
        new java.security.SecureRandom().nextBytes(auth);
        byte[] payload = "{\"type\":\"ENROLLMENT\"}".getBytes(StandardCharsets.UTF_8);

        byte[] message = WebPushEncryption.encrypt(payload, P256.encode((ECPublicKey) subscriber.getPublic()), auth);

        assertThat(decrypt(message, subscriber, auth)).isEqualTo(payload);
    }

    @Test
    void two_messages_never_share_salt_or_server_key() throws Exception {
        KeyPair subscriber = P256.generateKeyPair();
        byte[] publicKey = P256.encode((ECPublicKey) subscriber.getPublic());
        byte[] auth = new byte[16];

        byte[] first = WebPushEncryption.encrypt(new byte[]{1}, publicKey, auth);
        byte[] second = WebPushEncryption.encrypt(new byte[]{1}, publicKey, auth);

        assertThat(Arrays.copyOfRange(first, 0, 16)).isNotEqualTo(Arrays.copyOfRange(second, 0, 16));
        assertThat(Arrays.copyOfRange(first, 21, 86)).isNotEqualTo(Arrays.copyOfRange(second, 21, 86));
    }

    @Test
    void rejects_a_payload_that_does_not_fit_one_record() throws Exception {
        byte[] publicKey = P256.encode((ECPublicKey) P256.generateKeyPair().getPublic());

        assertThatThrownBy(() -> WebPushEncryption.encrypt(new byte[4080], publicKey, new byte[16]))
                .hasMessageContaining("does not fit");
    }

    /** The browser's side of RFC 8291, to read back what the server sent. */
    static byte[] decrypt(byte[] message, KeyPair subscriber, byte[] auth) throws Exception {
        ByteBuffer in = ByteBuffer.wrap(message);
        byte[] salt = new byte[16];
        in.get(salt);
        assertThat(in.getInt()).isEqualTo(WebPushEncryption.RECORD_SIZE);
        byte[] serverPublic = new byte[in.get()];
        in.get(serverPublic);
        byte[] record = new byte[in.remaining()];
        in.get(record);

        KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
        agreement.init((ECPrivateKey) subscriber.getPrivate());
        agreement.doPhase(P256.publicKey(serverPublic), true);
        byte[] subscriberPublic = P256.encode((ECPublicKey) subscriber.getPublic());
        byte[] info = ByteBuffer.allocate(14 + 65 + 65)
                .put("WebPush: info\0".getBytes(StandardCharsets.US_ASCII)).put(subscriberPublic).put(serverPublic).array();
        byte[] ikm = WebPushEncryption.hkdf(auth, agreement.generateSecret(), info, 32);
        byte[] key = WebPushEncryption.hkdf(salt, ikm, "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII), 16);
        byte[] nonce = WebPushEncryption.hkdf(salt, ikm, "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII), 12);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        byte[] padded = cipher.doFinal(record);
        assertThat(padded[padded.length - 1]).isEqualTo((byte) 0x02);
        return Arrays.copyOf(padded, padded.length - 1);
    }
}

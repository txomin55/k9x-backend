package com.k9x.infrastructure.out.push;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * The P-256 (secp256r1) keys Web Push is built on, as the JDK represents them. Browsers and the VAPID key pair
 * carry them as base64url bytes: a public key is the 65-byte uncompressed point {@code 0x04 || X || Y}, a private
 * key the 32-byte scalar.
 */
final class P256 {

    private static final String CURVE = "secp256r1";
    private static final int COORDINATE_BYTES = 32;
    private static final int UNCOMPRESSED_POINT_BYTES = 1 + 2 * COORDINATE_BYTES;
    private static final byte UNCOMPRESSED = 0x04;
    private static final ECParameterSpec PARAMETERS = parameters();

    private P256() {
    }

    static ECPublicKey publicKey(byte[] uncompressedPoint) throws GeneralSecurityException {
        if (uncompressedPoint.length != UNCOMPRESSED_POINT_BYTES || uncompressedPoint[0] != UNCOMPRESSED) {
            throw new GeneralSecurityException("Not an uncompressed P-256 point (" + uncompressedPoint.length + " bytes)");
        }
        ECPoint point = new ECPoint(
                new BigInteger(1, Arrays.copyOfRange(uncompressedPoint, 1, 1 + COORDINATE_BYTES)),
                new BigInteger(1, Arrays.copyOfRange(uncompressedPoint, 1 + COORDINATE_BYTES, UNCOMPRESSED_POINT_BYTES)));
        return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(point, PARAMETERS));
    }

    static ECPrivateKey privateKey(byte[] scalar) throws GeneralSecurityException {
        return (ECPrivateKey) KeyFactory.getInstance("EC")
                .generatePrivate(new ECPrivateKeySpec(new BigInteger(1, scalar), PARAMETERS));
    }

    static byte[] encode(ECPublicKey key) {
        byte[] encoded = new byte[UNCOMPRESSED_POINT_BYTES];
        encoded[0] = UNCOMPRESSED;
        writeCoordinate(key.getW().getAffineX(), encoded, 1);
        writeCoordinate(key.getW().getAffineY(), encoded, 1 + COORDINATE_BYTES);
        return encoded;
    }

    /** The 32-byte scalar of a private key, the form a VAPID private key is exchanged in. */
    static byte[] encode(ECPrivateKey key) {
        byte[] encoded = new byte[COORDINATE_BYTES];
        writeCoordinate(key.getS(), encoded, 0);
        return encoded;
    }

    static KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec(CURVE));
        return generator.generateKeyPair();
    }

    /** Accepts base64url or standard base64, padded or not: browsers and key generators differ. */
    static byte[] decodeBase64(String value) {
        String url = value.trim().replace('+', '-').replace('/', '_');
        int end = url.length();
        while (end > 0 && url.charAt(end - 1) == '=') {
            end--;
        }
        return Base64.getUrlDecoder().decode(url.substring(0, end));
    }

    static String encodeBase64(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /** Big-endian, left-padded to the coordinate size: {@link BigInteger#toByteArray} drops or adds a sign byte. */
    private static void writeCoordinate(BigInteger value, byte[] target, int offset) {
        byte[] raw = value.toByteArray();
        int start = Math.max(0, raw.length - COORDINATE_BYTES);
        int length = raw.length - start;
        System.arraycopy(raw, start, target, offset + COORDINATE_BYTES - length, length);
    }

    private static ECParameterSpec parameters() {
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec(CURVE));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("The JDK has no " + CURVE + " curve", e);
        }
    }
}

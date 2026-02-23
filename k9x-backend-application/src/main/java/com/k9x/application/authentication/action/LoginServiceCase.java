package com.k9x.application.authentication.action;

import com.k9x.application.authentication.command.LoginCommand;
import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.application.authentication.dto.LoginDTO;
import com.k9x.application.authentication.port.JwtTokenCacheManagerPort;
import com.k9x.application.authentication.port.ValidateIdTokenPort;
import com.k9x.domain.commons.exception.UnauthorizedResourceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

@Service
public class LoginServiceCase {

    private static final String ISSUER = "k9x-backend";
    private final ValidateIdTokenPort validateIdTokenPort;
    private final JwtTokenCacheManagerPort jwtTokenCacheManagerPort;
    private final byte[] jwtSecret;
    private final Duration jwtTtl;
    private final PublicKey googlePublicKey;

    public LoginServiceCase(
            ValidateIdTokenPort validateIdTokenPort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            @Value("${k9x-backend.security.jwt-secret}") String jwtSecret,
            @Value("${k9x-backend.security.jwt-public-jwk-n}") String jwtPublicJwkN,
            @Value("${k9x-backend.security.jwt-public-jwk-e}") String jwtPublicJwkE,
            @Value("${k9x-backend.security.jwt-cache-ttl-minutes}") long ttlMinutes
    ) {
        this.validateIdTokenPort = validateIdTokenPort;
        this.jwtTokenCacheManagerPort = jwtTokenCacheManagerPort;
        this.jwtSecret = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.jwtTtl = Duration.ofMinutes(ttlMinutes);
        this.googlePublicKey = parseRsaPublicKey(jwtPublicJwkN, jwtPublicJwkE);
    }

    public LoginDTO login(LoginCommand command) {
        boolean valid = validateIdTokenPort.isValid(command.idToken());
        if (!valid) {
            throw new UnauthorizedResourceException();
        }

        Claims tokenClaims = parseGoogleIdToken(command.idToken());
        String userEmail = tokenClaims.get("email").toString();

        AuthTokenDTO cachedData = jwtTokenCacheManagerPort.retrieveEntry(userEmail);

        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + jwtTtl.toMillis());
        String jwtToken = Jwts.builder()
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(Keys.hmacShaKeyFor(jwtSecret))
                .subject(userEmail)
                .audience().add(ISSUER).and()
                .claim("version", cachedData != null ? cachedData.getVersion() + 1 : 0)
                .compact();

        jwtTokenCacheManagerPort.overrideEntry(userEmail, jwtToken);

        return new LoginDTO(true, jwtToken);
    }

    private Claims parseGoogleIdToken(String idToken) {
        return Jwts.parser()
                .verifyWith(googlePublicKey)
                .build()
                .parseSignedClaims(idToken)
                .getPayload();
    }

    private PublicKey parseRsaPublicKey(String modulusBase64Url, String exponentBase64Url) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(modulusBase64Url);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(exponentBase64Url);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(
                    new BigInteger(1, modulusBytes),
                    new BigInteger(1, exponentBytes)
            );
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid RSA JWK format.", ex);
        }
    }

}

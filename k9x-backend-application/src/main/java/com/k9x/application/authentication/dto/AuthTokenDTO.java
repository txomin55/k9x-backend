package com.k9x.application.authentication.dto;

import java.util.Date;
import java.util.Set;

public class AuthTokenDTO {

    private final String subject;
    private final String issuer;
    private final Set<String> audience;
    private final Date issuedAt;
    private final int version;

    public AuthTokenDTO(String subject, String issuer, Set<String> audience, Date issuedAt, int version) {
        this.subject = subject;
        this.issuer = issuer;
        this.audience = audience;
        this.issuedAt = issuedAt;
        this.version = version;
    }

    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public Set<String> getAudience() {
        return audience;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public int getVersion() {
        return version;
    }
}

package com.k9x.infrastructure.in.rest.configuration.session.user.type.jwt;

import com.k9x.infrastructure.in.rest.configuration.session.user.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.user.dto.RequestUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Arrays;

public class JwtAuthorizationExtractor implements AuthorizationExtractor {

  @Override
  public RequestUserDetails getDataFromToken(String authorization) {
    String token = authorization.split(" ")[1];

    Claims claimsJws = Jwts
      .parser()
      .verifyWith(Keys.hmacShaKeyFor("secret".getBytes()))
      .build()
      .parseSignedClaims(token)
      .getPayload();

    String[] organizations = claimsJws.get("organizations").toString().split(",");
    return new RequestUserDetails(claimsJws.get("id").toString(), claimsJws.get("fullName").toString(), Arrays.asList(organizations));
  }
}

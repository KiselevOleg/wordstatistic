/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

/**
 * @author Kiselev Oleg
 * provides methods for generating, validating, and extracting
 * information from JSON Web Tokens (JWTs) used for
 * authentication in a Spring Boot application.
 */
@Component
public class JwtTokenProvider {
    private static final String JWT_SECRET = "daf66e01593f61a15b857cf433aae03a005812b31234e149036bcc8dee755dbb";
    private static final Long JWT_EXPIRATION_DATE = 604800000L;

    /**
     * generate JWT token.
     * @param authentication a authentication
     * @return a token
     */
    public String generateToken(final Authentication authentication) {
        final String username = authentication.getName();
        final Date currentDate = new Date();
        final Date expireDate = new Date(currentDate.getTime() + JWT_EXPIRATION_DATE);

        final String token = Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(expireDate)
            .signWith(key())
            .compact();

        return token;
    }
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }

    /**
     * get username from JWT token.
     * @param token a token
     * @return a username
     */
    public String getUsername(final String token) {
        return Jwts.parser()
            .verifyWith((SecretKey) key())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    /**
     * validate JWT token.
     * @param token a token
     * @return true if valid
     */
    public Boolean validateToken(final String token) {
        Jwts.parser()
            .verifyWith((SecretKey) key())
            .build()
            .parse(token);
        return true;

    }
}

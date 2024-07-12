/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Kiselev Oleg
 */
@Component
public class JwtTokenProvider {
    private static final String JWT_SECRET = "daf66e01593f61a15b857cf433aae03a005812b31234e149036bcc8dee755dbb";

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_PERMISSIONS = "permissions";

    /**
     * get jwt key from a secret word.
     * @return a jwt key
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }

    /**
     * get user's id from JWT token.
     * @param token a token
     * @return id
     */
    public UUID getId(final String token) {
        return UUID.fromString(Jwts.parser()
            .verifyWith((SecretKey) key())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject());
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
            .get(CLAIM_USERNAME, String.class);
    }

    /**
     * get username from JWT token.
     *
     * @param token a token
     * @return a username
     */
    public Set getPermissions(final String token) {
        return (Set) Jwts.parser()
            .verifyWith((SecretKey) key())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get(CLAIM_PERMISSIONS, List.class)
            .stream().collect(Collectors.toSet());
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

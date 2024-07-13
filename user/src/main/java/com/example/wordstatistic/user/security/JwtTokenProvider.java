/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.security;

import com.example.wordstatistic.user.model.Permission;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Kiselev Oleg
 * provides methods for generating, validating, and extracting
 * information from JSON Web Tokens (JWTs) used for
 * authentication in a Spring Boot application.
 */
@Component
public class JwtTokenProvider {
    private static final String JWT_SECRET = "daf66e01593f61a15b857cf433aae03a005812b31234e149036bcc8dee755dbb";
    private static final String JWT_REFRESH_SECRET = "xdsgfwet934y934rf98whefr98we3ur9843yt98hf03jrnoaf0n3va04rnvao34r";
    private static final Long JWT_EXPIRATION_ACCESS_DATE = 600000L;
    private static final Long JWT_EXPIRATION_REFRESH_DATE = 3600000L;

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_PERMISSIONS = "permissions";

    private final UserRepository userRepository;

    @Autowired
    public JwtTokenProvider(
        final UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    /**
     * generate JWT access token.
     * @param username a authentication username
     * @return a token
     */
    public String generateAccessToken(final String username) {
        final Date currentDate = new Date();
        final Date expireDate = new Date(currentDate.getTime() + JWT_EXPIRATION_ACCESS_DATE);

        final User user = userRepository.findByName(username).orElseThrow();

        final String token = Jwts.builder()
            .subject(user.getUuid().toString())
            .issuedAt(new Date())
            .expiration(expireDate)
            .claim(CLAIM_USERNAME, username)
            .claim(
                CLAIM_PERMISSIONS,
                user.getRole().getPermissions().stream().map(Permission::getName).collect(Collectors.toSet())
            )
            .signWith(key())
            .compact();

        return token;
    }
    /**
     * generate JWT refresh token.
     * @param username a authentication username
     * @return a token
     */
    public String generateRefreshToken(final String username) {
        final Date currentDate = new Date();
        final Date expireDate = new Date(currentDate.getTime() + JWT_EXPIRATION_REFRESH_DATE);

        final User user = userRepository.findByName(username).orElseThrow();

        final String token = Jwts.builder()
            .subject(user.getUuid().toString())
            .issuedAt(new Date())
            .expiration(expireDate)
            .signWith(refreshKey())
            .compact();

        return token;
    }

    /**
     * get jwt key from a secret word.
     * @return a jwt key
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }
    /**
     * get jwt key from a secret word for a refresh token.
     * @return a jwt key
     */
    private Key refreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_REFRESH_SECRET));
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
    /**
     * validate JWT refresh token.
     * @param refreshToken a refresh token
     * @return true if valid
     */
    public Boolean validateRefreshToken(final String refreshToken) {
        Jwts.parser()
            .verifyWith((SecretKey) refreshKey())
            .build()
            .parse(refreshToken);
        return true;
    }

    /**
     * get user's id from JWT refresh token.
     * @param refreshToken a refresh token
     * @return a username
     */
    public String getRefreshId(final String refreshToken) {
        return Jwts.parser()
            .verifyWith((SecretKey) refreshKey())
            .build()
            .parseSignedClaims(refreshToken)
            .getPayload()
            .getSubject();
    }
}

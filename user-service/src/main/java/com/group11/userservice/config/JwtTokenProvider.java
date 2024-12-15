package com.group11.userservice.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import com.group11.model.User;

import java.util.Date;

@Slf4j
public class JwtTokenProvider {

    private final long jwtExpiration;

    public JwtTokenProvider() {
        this.jwtExpiration = 3600000;
    }

    public String generateToken(User user) throws Exception {

        try {
            return Jwts.builder()
                    .setSubject(user.getId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date((new Date()).getTime() + jwtExpiration))
                    .signWith(SignatureAlgorithm.HS512, Keys.secretKeyFor(SignatureAlgorithm.HS512))
                    .compact();
        }catch (JwtException e) {
                log.error("JWT exception during token generation for user ID: {}", user.getId(), e);
                throw new Exception("Error generating JWT token for user ID: " + user.getId(), e);
            } catch (Exception e) {
                log.error("Unexpected error during token generation for user ID: {}", user.getId(), e);
                throw new Exception("Unexpected error during token generation", e);
            }

        }
}
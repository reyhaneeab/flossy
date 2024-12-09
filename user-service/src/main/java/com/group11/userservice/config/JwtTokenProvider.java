package com.group11.userservice.config;

import com.group11.model.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;

/**
 * This class provides functionality for generating, parsing, and validating JWT tokens.
 * It's used for handling user authentication in a stateless manner.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    // Secret key used for signing the JWT
    @Value("${jwt.secret-key}")
    private String jwtSecret;

    // Token expiration time in milliseconds
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generates a JWT token for a given user.
       user: The user for whom the token is being generated
       return A JWT token as a String
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())  // Use email as the subject
                .setIssuedAt(new Date())  // Set the token issue time
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))  // Set the expiration time
                .signWith(SignatureAlgorithm.HS512, jwtSecret)  // Sign the token with HS512 algorithm and secret key
                .compact();  // Build the token
    }

    /**
     * Extracts the user email from a JWT token.
      token :The JWT token
      return The email of the user
     */
    public String getUserEmailFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Validates a given JWT token.
       authToken : The token to validate
       return true if the token is valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
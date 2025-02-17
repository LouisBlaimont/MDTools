package be.uliege.speam.team03.MDTools.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtils {
      
   private final long jwtExpirationMs;

   private final SecretKey key;

   public JwtTokenUtils(@Value("${security.jwt.secret}") final String jwtSecret,@Value("${security.token.expiry-hours}") long jwtExpirationMs) {
      this.jwtExpirationMs = jwtExpirationMs;
      this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
   }

   public String generateToken(Authentication authentication) {
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      

      return Jwts.builder()
            .subject(userDetails.getUsername()).claim("roles", userDetails.getAuthorities())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key)
            .compact();
   }

    // Parse and validate the JWT token
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Validate the token
    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    // Extract the username from the token
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }
}

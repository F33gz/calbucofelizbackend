package cl.metspherical.calbucofelizbackend.service;

import cl.metspherical.calbucofelizbackend.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;

/**
 * Service responsible for JWT token operations including
 * generation, validation, and extraction of claims
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    /**
     * Creates and returns the signing key for JWT operations
     * 
     * @return SecretKey for signing tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Extracts the RUT (subject) from a JWT token
     * 
     * @param token JWT token string
     * @return RUT as string
     */
    public String extractRut(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID from a JWT token
     *
     * @param token JWT token string
     * @return User ID as UUID
     */
    public UUID extractUserId(String token) {
        String userIdString = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userIdString);
    }

    /**
     * Extracts a specific claim from the JWT token
     * 
     * @param <T> Type of the claim to extract
     * @param token JWT token string
     * @param claimsResolver Function to resolve the specific claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token
     * 
     * @param token JWT token string
     * @return Claims object containing all token claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if a JWT token has expired
     * 
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token
     * 
     * @param token JWT token string
     * @return Date when the token expires
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }    
    
    /**
     * Generates an access token for a user
     * 
     * @param user User entity for whom to generate the token
     * @return JWT access token string
     */
    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("username", user.getUsername());
        extraClaims.put("roles", user.getRoles());
        
        return createToken(extraClaims, user.getRut(), jwtExpiration);
    }

    /**
     * Generates a refresh token for a user
     * 
     * @param user User entity for whom to generate the token
     * @return JWT refresh token string
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("type", "refresh");
        
        return createToken(extraClaims, user.getRut(), jwtRefreshExpiration);
    }

    /**
     * Creates a JWT token with specified claims and expiration
     * 
     * @param extraClaims Additional claims to include in the token
     * @param subject Subject of the token (usually RUT)
     * @param expiration Expiration time in milliseconds
     * @return JWT token string
     */
    private String createToken(Map<String, Object> extraClaims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates if a JWT token is valid and not expired
     * 
     * @param token JWT token string to validate
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates if a refresh token is valid and properly formatted
     * 
     * @param token Refresh token string to validate
     * @return true if refresh token is valid, false otherwise
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
}
package com.example.messenger.service;

import com.example.messenger.dto.request.RegisterRequest;
import com.example.messenger.entity.Token.RefreshTokenEntity;
import com.example.messenger.entity.user.Role;
import com.example.messenger.entity.user.UserEntity;
import com.example.messenger.entity.user.UserInfoEntity;
import com.example.messenger.entity.user.UserPropertiesEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class JWTProvider {
    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    private final Long jwtAccessTokenExpiration;
    private final Long jwtRefreshTokenExpiration;
    private final PasswordEncoder passwordEncoder;

    public JWTProvider(
            @Value("${jwt.secret.access-token}") String jwtAccessSecret,
            @Value("${jwt.secret.refresh-token}") String jwtRefreshSecret,
            @Value("${jwt.expiration.access-token}") Long jwtAccessTokenExpiration,
            @Value("${jwt.expiration.refresh-token}") Long jwtRefreshTokenExpiration,
            PasswordEncoder passwordEncoder) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
        this.jwtAccessTokenExpiration = jwtAccessTokenExpiration;
        this.jwtRefreshTokenExpiration = jwtRefreshTokenExpiration;
        this.passwordEncoder = passwordEncoder;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails,
                                      UUID userId,
                                      String sessionId) {
        return generateAccessToken(new HashMap<>(), userDetails, userId, sessionId);
    }

    public String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            UUID userId,
            String sessionId
    ) {
        extraClaims.put("sessionId", sessionId);
        extraClaims.put("username", userDetails.getUsername());
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userId.toString())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtAccessTokenExpiration))
                .signWith(jwtAccessSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails,
                                       UUID userId,
                                       String sessionId) {
        return generateRefreshToken(new HashMap<>(), userDetails, userId, sessionId);
    }

    public String generateRefreshToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            UUID userId,
            String sessionId
    ) {
        extraClaims.put("sessionId", sessionId);
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshTokenExpiration))
                .signWith(jwtRefreshSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(jwtAccessSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean validateToken(String token, SecretKey secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Token expired", expEx);
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt", unsEx);
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt", mjEx);
        } catch (Exception e) {
            log.error("invalid token", e);
        }
        return false;
    }

    private Claims getClaims(String token, SecretKey secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims getAccessClaims(String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, jwtAccessSecret);
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, jwtRefreshSecret);
    }

    public UserEntity buildUserEntity(RegisterRequest request) {
        return UserEntity.builder()
                .phoneNumber(request.getPhoneNumber())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .userInfoEntity(UserInfoEntity.builder()
                        .name(request.getName())
                        .surname(request.getSurname())
                        .email(request.getEmail())
                        .status(request.getStatus())
                        .build())
                .userPropertiesEntity(UserPropertiesEntity.builder()
                        .countMessages(0L)
                        .isEmailVerified(false)
                        .isPhoneNumberVerified(false)
                        .isOathVerified(false)
                        .isBlocked(false)
                        .isAcceptedMessage(true)
                        .isAcceptedVideoCall(true)
                        .isAcceptedVoiceCall(true)
                        .build())
                .role(Role.USER)
                .build();
    }

    public String generateSessionID(String ipAddress, String userAgent) {
        String salt = UUID.randomUUID().toString();
        return passwordEncoder.encode(ipAddress + userAgent + salt).substring(14);
    }

    public RefreshTokenEntity buildRedisRefreshTokenEntity(String refreshToken, UUID userId, String sessionId) {
        return RefreshTokenEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .refreshToken(refreshToken)
                .ttl(jwtRefreshTokenExpiration / 1000)
                .build();
    }

    public ResponseCookie getAccessTokenCookie(String accessToken) {
        return ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(jwtAccessTokenExpiration / 1000)
                .build();
    }

    public ResponseCookie getRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(jwtRefreshTokenExpiration / 1000)
                .build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
    }

}

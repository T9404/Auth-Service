package com.example.messenger.service;

import com.example.messenger.entity.Token.RefreshTokenEntity;
import com.example.messenger.exception.DBException;
import com.example.messenger.exception.ObjectNotExists;
import com.example.messenger.repository.RefreshTokenRepo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final RefreshTokenRepo tokenRepository;
    private final JWTProvider jwtProvider;

    @SneakyThrows
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws RuntimeException {
        String accessToken = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("accessToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if (accessToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new RuntimeException("Authorization cookie is missing or invalid");
        }
        final Claims claims = jwtProvider.extractAllClaims(accessToken);
        final UUID userID = UUID.fromString(claims.getSubject());
        final String sessionID = claims.get("sessionId", String.class);
        RefreshTokenEntity saveRefreshToken;
        try {
            saveRefreshToken = tokenRepository.findByUserIdAndSessionId(userID, sessionID);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new ObjectNotExists("Refresh token not found");
        }
        try {
            tokenRepository.deleteById(saveRefreshToken.getId());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new DBException("Can't delete refresh token");
        }
        SecurityContextHolder.clearContext();
        ResponseCookie clearAccess = jwtProvider.clearAccessTokenCookie();
        ResponseCookie clearRefresh = jwtProvider.clearRefreshTokenCookie();
        response.addHeader("Set-Cookie", clearAccess.toString());
        response.addHeader("Set-Cookie", clearRefresh.toString());
    }
}

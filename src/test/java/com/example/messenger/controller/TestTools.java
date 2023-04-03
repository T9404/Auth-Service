package com.example.messenger.controller;

import com.example.messenger.dto.response.AuthResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class TestTools {
    public HttpHeaders getHandlerForCookieDefault(ResponseEntity<AuthResponse> response) {
        return getHandlerForCookie(response, 1296000L);
    }

    public HttpHeaders getHandlerForCookieMaxAge(ResponseEntity<AuthResponse> response, Long maxAgeRefreshToken) {
        return getHandlerForCookie(response, maxAgeRefreshToken);
    }

    private HttpHeaders getHandlerForCookie(ResponseEntity<AuthResponse> response, Long maxAgeRefreshToken) {
        HttpHeaders headers = new HttpHeaders();
        List<String> setCookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookieHeaders != null) {
            for (String setCookieHeader : setCookieHeaders) {
                if (setCookieHeader.startsWith("refreshToken") && maxAgeRefreshToken != -1L) {
                    Cookie cookie = new Cookie("refreshToken", setCookieHeader);
                    headers.add(HttpHeaders.COOKIE, cookie.getValue());
                }
                if (setCookieHeader.startsWith("accessToken")) {
                    Cookie cookie = new Cookie("accessToken", setCookieHeader);
                    headers.add(HttpHeaders.COOKIE, cookie.getValue());
                }
            }
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

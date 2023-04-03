package com.example.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private ResponseCookie accessToken;
    private ResponseCookie refreshToken;
}

package com.example.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AuthResponseWithAllowCode {
    private ResponseCookie accessToken;
    private ResponseCookie refreshToken;
    private boolean isVerifiedEmail;
    private UUID idTemporaryData;
}

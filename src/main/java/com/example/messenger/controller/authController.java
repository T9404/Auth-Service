package com.example.messenger.controller;

import com.example.messenger.dto.request.LoginRequest;
import com.example.messenger.dto.request.RegisterRequest;
import com.example.messenger.dto.request.VerifyCodeRequest;
import com.example.messenger.dto.response.AuthResponse;
import com.example.messenger.dto.response.AuthResponseWithAllowCode;
import com.example.messenger.service.AuthenticationService;
import com.example.messenger.service.ControllerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping(method = RequestMethod.PUT, consumes = MediaType.ALL_VALUE, path = "/users")
public class authController {

    private final AuthenticationService authService;
    private final ControllerService controllerService;

    @ModelAttribute
    public void setResponseHeader(HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request,
            @NonNull HttpServletRequest httpServletRequest
    ) throws Exception {
        final HashMap<String, Object> userDevice = controllerService.getUserDevice(httpServletRequest);
        request.setIpAddress((String) userDevice.get("ipAddress"));
        request.setUserAgent((String) userDevice.get("userAgent"));
        AuthResponse tokens = authService.register(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.getAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, tokens.getRefreshToken().toString())
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            @NonNull HttpServletRequest httpServletRequest
    ) throws Exception {
        final HashMap<String, Object> userDevice = controllerService.getUserDevice(httpServletRequest);
        request.setIpAddress((String) userDevice.get("ipAddress"));
        request.setUserAgent((String) userDevice.get("userAgent"));
        final AuthResponseWithAllowCode tokens = authService.login(request, controllerService.getUserSystemInfo(httpServletRequest));
        if (tokens.isVerifiedEmail()) {
            return ResponseEntity.ok(String.format("redirect:/users/verify?id=%s", tokens.getIdTemporaryData()));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.getAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, tokens.getRefreshToken().toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> getNewRefreshToken(
            @CookieValue("refreshToken") String refreshToken
    ) throws Exception {
        final AuthResponse tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.getAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, tokens.getRefreshToken().toString())
                .build();
    }

    @PostMapping("/token")
    public ResponseEntity<?> getNewAccessToken(
            @CookieValue("refreshToken") String refreshToken
    ) throws Exception {
        final AuthResponse token = authService.getAccessToken(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, token.getAccessToken().toString())
                .build();
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestParam String id,
            @RequestBody VerifyCodeRequest request
    ) throws Exception {
        final AuthResponse tokens = authService.verify(request, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.getAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, tokens.getRefreshToken().toString())
                .build();
    }

    @PostMapping("/confirmEmail")
    public ResponseEntity<?> confirmEmail(
            @CookieValue("accessToken") String accessToken
    ) throws Exception {
        if (authService.confirmEmail(accessToken)) {
            return ResponseEntity.ok("Письмо отправлено на почту");
        }
        return ResponseEntity.badRequest().body("Произошла ошибка");
    }

    @GetMapping("/acceptEmail")
    public ResponseEntity<?> checkSentLink(
            @RequestParam String code
    ) throws Exception {
        if (authService.checkSentToEmailLink(code)) {
            return ResponseEntity.ok("redirect:/main");
        }
        return ResponseEntity.badRequest().body("Произошла ошибка");
    }
}

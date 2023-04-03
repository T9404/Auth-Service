package com.example.messenger.service;

import com.example.messenger.dto.UserSystemInfoDto;
import com.example.messenger.dto.request.LoginRequest;
import com.example.messenger.dto.request.RegisterRequest;
import com.example.messenger.dto.request.VerifyCodeRequest;
import com.example.messenger.dto.response.AuthResponse;
import com.example.messenger.dto.response.AuthResponseWithAllowCode;
import com.example.messenger.entity.Keys.VerifyEmailEntity;
import com.example.messenger.entity.Token.RefreshTokenEntity;
import com.example.messenger.entity.Token.TemporaryUserDataEntity;
import com.example.messenger.entity.user.UserEntity;
import com.example.messenger.exception.DBException;
import com.example.messenger.exception.ObjectAlreadyExists;
import com.example.messenger.exception.ObjectInvalidException;
import com.example.messenger.exception.ObjectNotExists;
import com.example.messenger.repository.RefreshTokenRepo;
import com.example.messenger.repository.TemporaryUserDataRepo;
import com.example.messenger.repository.UserRepo;
import com.example.messenger.repository.VerifyEmailRepo;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Validated
public class AuthenticationService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTProvider jwtProvider;
    private final RefreshTokenRepo refreshTokenRepo;
    private final TemporaryUserDataRepo temporaryUserDataRepo;
    private final DefaultEmailService emailService;
    private final VerifyEmailRepo verifyEmailRepo;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public AuthResponse register(@Valid RegisterRequest request) throws ObjectAlreadyExists, DBException {
        if (userRepo.existsByPhoneNumberOrUsername(request.getPhoneNumber(), request.getUsername())) {
            throw new ObjectAlreadyExists("User already exists");
        }
        final var user = jwtProvider.buildUserEntity(request);
        final UUID userID = userRepo.save(user).getId();
        final String sessionID = jwtProvider.generateSessionID(request.getIpAddress(), request.getUserAgent());
        final var jwtRefreshToken = jwtProvider.generateRefreshToken(user, userID, sessionID);
        try {
            refreshTokenRepo.save(jwtProvider.buildRedisRefreshTokenEntity(jwtRefreshToken, user.getId(), sessionID));
        } catch (Exception e) {
            throw new DBException("Can't save refresh token");
        }
        final var jwtAccessToken = jwtProvider.generateAccessToken(user, userID, sessionID);
        return AuthResponse.builder()
                .accessToken(jwtProvider.getAccessTokenCookie(jwtAccessToken))
                .refreshToken(jwtProvider.getRefreshTokenCookie(jwtRefreshToken))
                .build();
    }

    public AuthResponseWithAllowCode login(@Valid LoginRequest request, UserSystemInfoDto userSystemInfoDto) throws ObjectNotExists, ObjectInvalidException, DBException {
        final UserEntity user = userRepo.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ObjectNotExists("User not found"));
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            final var refreshToken = jwtProvider.generateRefreshToken(user, user.getId(),
                    jwtProvider.generateSessionID(request.getIpAddress(), request.getUserAgent()));
            final var sessionID = jwtProvider.generateSessionID(request.getIpAddress(), request.getUserAgent());
            final var accessToken = jwtProvider.generateAccessToken(user, user.getId(), sessionID);
            try {
                refreshTokenRepo.save(jwtProvider.buildRedisRefreshTokenEntity(refreshToken, user.getId(), sessionID));
            } catch (Exception e) {
                throw new DBException("Can't save refresh token");
            }
            if (user.getUserPropertiesEntity().getIsEmailVerified()) {
                AuthResponseWithAllowCode response = AuthResponseWithAllowCode.builder()
                        .accessToken(jwtProvider.getAccessTokenCookie(accessToken))
                        .refreshToken(jwtProvider.getRefreshTokenCookie(refreshToken))
                        .isVerifiedEmail(true)
                        .idTemporaryData(null)
                        .build();
                final var idTemporaryData = getVerifyCodeAndSendEmail(response,
                        user.getUserInfoEntity().getEmail(),
                        userSystemInfoDto);
                response.setIdTemporaryData(idTemporaryData);
                return response;
            }
            return AuthResponseWithAllowCode.builder()
                    .accessToken(jwtProvider.getAccessTokenCookie(accessToken))
                    .refreshToken(jwtProvider.getRefreshTokenCookie(refreshToken))
                    .isVerifiedEmail(false)
                    .idTemporaryData(null)
                    .build();
        } else {
            throw new ObjectInvalidException("Invalid password");
        }
    }

    public AuthResponse verify(@Valid VerifyCodeRequest request, String idTemporaryData) throws ObjectInvalidException, DBException {
        Optional<TemporaryUserDataEntity> data;
        try {
            data = temporaryUserDataRepo.findById(UUID.fromString(idTemporaryData));
            if (!data.isPresent()) {
                throw new ObjectInvalidException("Invalid code");
            }
        } catch (Exception e) {
            throw new ObjectInvalidException("Invalid code");
        }
        if (!data.get().getCodeEmail().equals(request.getVerifyCode())) {
            throw new ObjectInvalidException("Invalid code");
        }
        try {
            temporaryUserDataRepo.deleteById(UUID.fromString(idTemporaryData));
        } catch (Exception e) {
            throw new DBException("Database isn't available");
        }
        return AuthResponse.builder()
                .accessToken(data.get().getAccessToken())
                .refreshToken(data.get().getRefreshToken())
                .build();
    }


    public boolean confirmEmail(String accessToken) throws ObjectNotExists {
        final var userId = jwtProvider.getAccessClaims(accessToken).getSubject();
        final var user = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ObjectNotExists("User not found"));

        if (user.getUserInfoEntity().getEmail() != null) {
            getVerifyLinkAndSendEmail(user.getUserInfoEntity().getEmail(), user.getId(), user.getUsername());
            return true;
        } else {
            throw new ObjectNotExists("Email isn't present");
        }
    }

    public boolean checkSentToEmailLink(String code) throws Exception {
        try {
            final var link = verifyEmailRepo.findById(UUID.fromString(code));
            final var user = userRepo.findById(link.get().getUserId());
            if (user.isPresent()) {
                user.get().getUserPropertiesEntity().setIsEmailVerified(true);
                userRepo.save(user.get());
                verifyEmailRepo.deleteById(UUID.fromString(code));
                return true;
            } else {
                throw new ObjectNotExists("User not found");
            }
        } catch (Exception e) {
            throw new DBException("Database isn't available");
        }

    }

    public void getVerifyLinkAndSendEmail(String email, UUID userId, String username) {
        final UUID code = UUID.randomUUID();
        final String link = "http://localhost:8080/users/acceptEmail?code=" + code.toString();
        verifyEmailRepo.save(VerifyEmailEntity.builder()
                .linkId(code)
                .userId(userId)
                .ttl(900L)
                .build());
        executorService.submit(() -> {
            try {
                emailService.sendHtmlEmail(email,
                        "Verify email",
                        getFormForVerificationLink(username, link));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String getFormForVerificationLink(String username, String link) {
        String path = "src/main/resources/ConfirmEmailForm.html";
        String form = readFile(path);
        return form.replace("{{username}}", username)
                .replace("{{link}}", link);
    }


    public AuthResponse refresh(String refreshToken) throws ObjectNotExists, ObjectInvalidException, DBException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String sessionID = claims.get("sessionId", String.class);
            final UUID userID = UUID.fromString(claims.getSubject());
            RefreshTokenEntity saveRefreshToken;
            try {
                saveRefreshToken = refreshTokenRepo.findByUserIdAndSessionId(userID, sessionID);
            } catch (Exception e) {
                throw new ObjectNotExists("Refresh token not found");
            }
            if (saveRefreshToken != null && saveRefreshToken.getRefreshToken().equals(refreshToken)) {
                final UserEntity user = userRepo.findById(userID)
                        .orElseThrow(() -> new ObjectNotExists("User not found"));
                final var newRefreshToken = jwtProvider.generateRefreshToken(user, userID, sessionID);
                try {
                    refreshTokenRepo.deleteById(saveRefreshToken.getId());
                } catch (Exception e) {
                    throw new DBException("Can't delete refresh token");
                }
                try {
                    refreshTokenRepo.save(jwtProvider.buildRedisRefreshTokenEntity(newRefreshToken, userID, sessionID));
                } catch (Exception e) {
                    throw new DBException("Can't save refresh token");
                }
                final var accessToken = jwtProvider.generateAccessToken(user, userID, sessionID);
                return AuthResponse.builder()
                        .accessToken(jwtProvider.getAccessTokenCookie(accessToken))
                        .refreshToken(jwtProvider.getRefreshTokenCookie(newRefreshToken))
                        .build();
            }
        }
        throw new ObjectInvalidException("Invalid refresh token");
    }

    public AuthResponse getAccessToken(String refreshToken) throws ObjectNotExists, ObjectInvalidException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final UUID userID = UUID.fromString(claims.getSubject());
            final String sessionID = claims.get("sessionId", String.class);
            RefreshTokenEntity saveRefreshToken;
            try {
                saveRefreshToken = refreshTokenRepo.findByUserIdAndSessionId(userID, sessionID);
            } catch (Exception e) {
                throw new ObjectNotExists("Refresh token not found");
            }
            if (saveRefreshToken.getRefreshToken() != null && saveRefreshToken.getRefreshToken().equals(refreshToken)) {
                final UserEntity user = userRepo.findById(userID)
                        .orElseThrow(() -> new ObjectNotExists("User not found"));
                final var accessToken = jwtProvider.generateAccessToken(user, userID, sessionID);
                return AuthResponse.builder()
                        .accessToken(jwtProvider.getAccessTokenCookie(accessToken))
                        .build();
            }
        }
        throw new ObjectInvalidException("Invalid refresh token");
    }

    public UUID getVerifyCodeAndSendEmail(AuthResponseWithAllowCode response, String email, UserSystemInfoDto userSystemInfoDto) {
        final String code = generateVerifyCode();
        final UUID id = saveTemporaryUserData(response, code);
        executorService.submit(() -> {
            try {
                emailService.sendHtmlEmail(email,
                        "Verify code",
                        getFormForVerificationCode(userSystemInfoDto, code, email));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });
        return id;
    }

    public static String readFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private String getFormForVerificationCode(UserSystemInfoDto userSystemInfoDto, String code, String email) {
        String path = "src/main/resources/VerifyCodeForm.html";
        String browser = userSystemInfoDto.getBrowser();
        String os = userSystemInfoDto.getOs();
        String ip = userSystemInfoDto.getIpAddress();
        String location = userSystemInfoDto.getLocation();
        String device = userSystemInfoDto.getDevice();
        String form = readFile(path);
        return form.replace("{{code}}", code)
                .replace("{{browser}}", browser)
                .replace("{{os}}", os)
                .replace("{{ip}}", ip)
                .replace("{{location}}", location)
                .replace("{{device}}", device)
                .replace("{{email}}", email);
    }

    private String generateVerifyCode() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int len = 6;
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, len)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(randomIndex -> String.valueOf(chars.charAt(randomIndex)))
                .collect(Collectors.joining());
    }

    private UUID saveTemporaryUserData(AuthResponseWithAllowCode response, String verifyCode) {
        TemporaryUserDataEntity entity = temporaryUserDataRepo.save(TemporaryUserDataEntity.builder()
                .refreshToken(response.getRefreshToken())
                .accessToken(response.getAccessToken())
                .codeEmail(verifyCode)
                .ttl(60L)
                .build());
        return entity.getId();
    }
}

package com.example.messenger.entity.Token;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.http.ResponseCookie;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash("TempData")
public class TemporaryUserDataEntity {
    @Id
    private UUID id;
    private ResponseCookie accessToken;
    private ResponseCookie refreshToken;
    private String codeEmail;
    @TimeToLive
    private Long ttl;
}

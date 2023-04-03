package com.example.messenger.entity.Token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash("RefreshToken")
public class RefreshTokenEntity {
    @Id
    private UUID id;
    private String refreshToken;
    @Indexed
    private UUID userId;
    @Indexed
    private String sessionId;
    @TimeToLive
    private Long ttl;
}

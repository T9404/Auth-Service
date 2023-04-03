package com.example.messenger.entity.Keys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash("VerifyEmail")
public class VerifyEmailEntity {
    @Id
    private UUID linkId;
    private UUID userId;
    @TimeToLive
    private Long ttl;
}

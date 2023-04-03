package com.example.messenger.repository;

import com.example.messenger.entity.Token.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenRepo extends CrudRepository<RefreshTokenEntity, UUID> {
    RefreshTokenEntity findByRefreshToken(String refreshToken);

    RefreshTokenEntity findByUserIdAndSessionId(UUID userId, String sessionId);

    List<RefreshTokenEntity> findAllByUserId(UUID userId);

    void deleteByUserIdAndSessionId(UUID userId, String sessionId);

    boolean existsByUserIdAndSessionId(UUID userId, String sessionId);
}

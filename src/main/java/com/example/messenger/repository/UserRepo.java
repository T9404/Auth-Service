package com.example.messenger.repository;

import com.example.messenger.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByPhoneNumberAndPassword(String phoneNumber, String password);

    boolean existsByPhoneNumberOrUsername(String phoneNumber, String username);

}

package com.example.messenger.repository;

import com.example.messenger.entity.Keys.VerifyEmailEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface VerifyEmailRepo extends CrudRepository<VerifyEmailEntity, UUID> {

}

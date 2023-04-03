package com.example.messenger.repository;

import com.example.messenger.entity.Token.TemporaryUserDataEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TemporaryUserDataRepo extends CrudRepository<TemporaryUserDataEntity, UUID> {

}

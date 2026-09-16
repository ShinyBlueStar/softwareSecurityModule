package com.sample.system.ssm.service.dataaccess.repository;

import com.sample.system.ssm.service.dataaccess.entity.command.CardSecretEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CardSecretCommandJpaRepository extends JpaRepository<CardSecretEntity, UUID> {

    @Query("SELECT c FROM CardSecretEntity c WHERE c.cardId = :cardId " +
           "AND c.secretType = :secretType AND c.status = :status")
    Optional<CardSecretEntity> findByCardIdAndSecretTypeAndStatus(
        @Param("cardId") UUID cardId,
        @Param("secretType") String secretType,
        @Param("status") String status
    );
}


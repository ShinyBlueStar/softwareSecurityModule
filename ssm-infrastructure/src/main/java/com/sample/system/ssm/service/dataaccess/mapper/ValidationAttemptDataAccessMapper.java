package com.sample.system.ssm.service.dataaccess.mapper;

import com.sample.system.ssm.service.dataaccess.entity.command.ValidationAttemptEntity;
import com.sample.system.ssm.service.domain.model.ValidationAttempt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ValidationAttemptDataAccessMapper {

    public ValidationAttemptEntity toEntity(ValidationAttempt domain) {
        log.info("ValidationAttemptDataAccessMapper.toEntity domain={}", domain);
        if (domain == null) return null;

        var entity = new ValidationAttemptEntity();
        if (domain.attemptId() != null) {
            entity.setAttemptId(domain.attemptId());
        }
        entity.setReferenceId(domain.referenceId());
        entity.setAttemptType(domain.attemptType());
        entity.setResult(domain.result());
        entity.setSourceIp(domain.sourceIp().orElse(null));
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }

    public ValidationAttempt toDomain(ValidationAttemptEntity entity) {
        log.info("ValidationAttemptDataAccessMapper.toDomain entity={}", entity);
        if (entity == null) return null;

        return new ValidationAttempt(
            entity.getAttemptId(),
            entity.getReferenceId(),
            entity.getAttemptType(),
            entity.getResult(),
            Optional.ofNullable(entity.getSourceIp()),
            entity.getCreatedAt()
        );
    }
}
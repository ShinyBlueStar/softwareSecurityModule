package com.sample.system.ssm.service.dataaccess.adapter;

import com.sample.system.ssm.service.dataaccess.mapper.ValidationAttemptDataAccessMapper;
import com.sample.system.ssm.service.dataaccess.repository.ValidationAttemptCommandJpaRepository;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.model.ValidationAttempt;
import com.sample.system.ssm.service.domain.ports.output.repository.ValidationAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationAttemptRepositoryImpl implements ValidationAttemptRepository {

    private final ValidationAttemptCommandJpaRepository jpa;
    private final ValidationAttemptDataAccessMapper mapper;

    @Override
    public ValidationAttempt save(ValidationAttempt attempt) {
        log.info("ValidationAttemptRepositoryImpl.save started, referenceId={}, attemptType={}", attempt != null ? attempt.referenceId() : null, attempt != null ? attempt.attemptType() : null);
        var entity = mapper.toEntity(attempt);
        var saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public long countFailedAttempts(String referenceId, SecretType attemptType, Instant since) {
        log.info("ValidationAttemptRepositoryImpl.countFailedAttempts started, referenceId={}, attemptType={}", referenceId, attemptType);
        if (attemptType == null) {
            log.error("ValidationAttemptRepositoryImpl.countFailedAttempts: attemptType is null");
            throw new IllegalArgumentException("attemptType must not be null");
        }
        try {
            return jpa.countFailedAttempts(referenceId, attemptType, since);
        } catch (Exception e) {
            log.error("ValidationAttemptRepositoryImpl.countFailedAttempts failed, referenceId={}, attemptType={}, error={}", referenceId, attemptType, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ValidationAttempt> findByReferenceIdAndType(String referenceId, SecretType attemptType) {
        log.info("ValidationAttemptRepositoryImpl.findByReferenceIdAndType started, referenceId={}, attemptType={}", referenceId, attemptType);
        if (attemptType == null) {
            log.error("ValidationAttemptRepositoryImpl.findByReferenceIdAndType: attemptType is null");
            throw new IllegalArgumentException("attemptType must not be null");
        }
        return jpa.findByReferenceIdAndAttemptType(referenceId, attemptType)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
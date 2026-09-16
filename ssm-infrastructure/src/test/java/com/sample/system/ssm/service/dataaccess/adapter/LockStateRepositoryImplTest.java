package com.sample.system.ssm.service.dataaccess.adapter;

import com.sample.system.ssm.service.dataaccess.entity.command.LockStateEntity;
import com.sample.system.ssm.service.dataaccess.mapper.LockStateDataAccessMapper;
import com.sample.system.ssm.service.dataaccess.repository.LockStateCommandJpaRepository;
import com.sample.system.ssm.service.domain.enums.LockReason;
import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.enums.LockStatus;
import com.sample.system.ssm.service.domain.model.LockState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LockStateRepositoryImplTest {

    @Mock
    private LockStateCommandJpaRepository jpa;

    @Mock
    private LockStateDataAccessMapper mapper;

    @InjectMocks
    private LockStateRepositoryImpl repository;

    @Test
    void release_findsAndReturnsReleasedLock_beforeDeletingTheRow() {
        UUID lockId = UUID.randomUUID();
        LockStateEntity entity = LockStateEntity.builder()
                .lockId(lockId)
                .lockScope(LockScope.CARD)
                .referenceId("card-123")
                .reason(LockReason.RETRY_LIMIT_EXCEEDED)
                .status(LockStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        LockState domain = new LockState(
                lockId, LockScope.CARD, "card-123", LockReason.RETRY_LIMIT_EXCEEDED,
                Optional.empty(), LockStatus.ACTIVE, Instant.now(), Optional.empty()
        );

        when(jpa.findById(lockId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        LockState result = repository.release(lockId);

        assertThat(result.status()).isEqualTo(LockStatus.RELEASED);
        assertThat(result.releasedAt()).isPresent();

        // The row must be looked up (and the release computed) BEFORE it is deleted —
        // regression test for the bug where deleteById() ran first, making the
        // subsequent findById() always fail with "Lock not found".
        InOrder order = inOrder(jpa);
        order.verify(jpa).findById(lockId);
        order.verify(jpa).deleteById(lockId);
    }

    @Test
    void release_throwsAndDoesNotDelete_whenLockDoesNotExist() {
        UUID lockId = UUID.randomUUID();
        when(jpa.findById(lockId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repository.release(lockId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(lockId.toString());

        verify(jpa, never()).deleteById(lockId);
    }
}

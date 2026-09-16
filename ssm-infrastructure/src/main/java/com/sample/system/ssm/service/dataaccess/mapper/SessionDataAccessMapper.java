package com.sample.system.ssm.service.dataaccess.mapper;

import com.sample.system.ssm.service.dataaccess.entity.command.SessionCommandEntity;
import com.sample.system.ssm.service.domain.enums.Channel;
import com.sample.system.ssm.service.domain.enums.SessionStatus;
import com.sample.system.ssm.service.domain.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SessionDataAccessMapper {

    public SessionCommandEntity toEntity(Session domain) {
        log.info("SessionDataAccessMapper.toEntity domain={}", domain);
        if (domain == null) return null;

        SessionCommandEntity entity = new SessionCommandEntity();
        entity.setSessionId(domain.getSessionId());
        entity.setCardId(domain.getCardId() != null ? domain.getCardId() : new UUID(0L, 0L));
        entity.setChannel(domain.getChannel() != null ? Channel.valueOf(domain.getChannel()) : Channel.API);
        entity.setSessionType(domain.getSessionType() != null ? domain.getSessionType() : "DEFAULT");
        entity.setStatus(domain.getStatus() != null ? domain.getStatus() : SessionStatus.ACTIVE);
        entity.setClientFingerprint(domain.getClientFingerprint());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpireAt(domain.getExpiresAt());
        return entity;
    }

    public Session toDomain(SessionCommandEntity entity) {
        log.info("SessionDataAccessMapper.toDomain domain={}", entity);
        if (entity == null) return null;

        return new Session(
                entity.getSessionId(),
                entity.getCardId(),
                entity.getChannel() != null ? entity.getChannel().name() : null,
                entity.getSessionType(),
                entity.getStatus(),
                entity.getClientFingerprint(),
                entity.getCreatedAt(),
                entity.getExpireAt()
        );
    }
}

package com.sample.system.ssm.service.dataaccess.mapper;

import com.sample.system.ssm.service.dataaccess.entity.command.StatusCommandEntity;
import com.sample.system.ssm.service.domain.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatusDataAccessMapper {

    public Status toDomain(StatusCommandEntity entity) {
        log.debug("StatusDataAccessMapper.toDomain started, id={}", entity != null ? entity.getId() : null);
        if (entity == null) return null;

        Status status = new Status(
                entity.getPersianDescription(),
                entity.getCode(),
                entity.getDescription()
        );
        status.setId(entity.getId());
        return status;
    }

    public StatusCommandEntity toEntity(Status domain) {
        log.debug("StatusDataAccessMapper.toEntity started, code={}", domain != null ? domain.getCode() : null);
        if (domain == null) return null;

        StatusCommandEntity entity = new StatusCommandEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setPersianDescription(domain.getPersianDescription());
        return entity;
    }
}

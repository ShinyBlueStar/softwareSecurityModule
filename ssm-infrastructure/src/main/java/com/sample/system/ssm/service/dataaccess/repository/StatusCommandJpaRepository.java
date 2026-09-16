package com.sample.system.ssm.service.dataaccess.repository;

import com.sample.system.ssm.service.dataaccess.entity.command.StatusCommandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusCommandJpaRepository extends JpaRepository<StatusCommandEntity, Long>,
        JpaSpecificationExecutor<StatusCommandEntity> {

    StatusCommandEntity findByCode(String code);
}

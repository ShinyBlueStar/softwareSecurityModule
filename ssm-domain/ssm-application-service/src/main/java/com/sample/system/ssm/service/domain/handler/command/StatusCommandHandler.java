package com.sample.system.ssm.service.domain.handler.command;

import com.sample.system.ssm.service.domain.command.status.CreateStatusCommand;
import com.sample.system.ssm.service.domain.command.status.UpdateStatusCommand;
import com.sample.system.ssm.service.domain.model.Status;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.mapper.StatusDataMapper;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.response.status.CreateStatusResponse;
import com.sample.system.ssm.service.domain.response.status.UpdateStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusCommandHandler {

    private final StatusService statusService;
    private final StatusDataMapper statusDataMapper;

    @Transactional
    public CreateStatusResponse createStatus(CreateStatusCommand command) throws SsmDomainException {
        log.info("Creating status with code: {}", command.getCode());
        Status status = statusDataMapper.createCommandToStatus(command);
        Status createdStatus = statusService.createStatus(status);
        return statusDataMapper.statusToCreateResponse(createdStatus, "Status created successfully");
    }

    @Transactional
    public UpdateStatusResponse updateStatusPersianDescription(UpdateStatusCommand command)
            throws SsmDomainException {
        log.info("Updating status persianDescription for code: {}", command.getCode());
        Status status = statusDataMapper.updateCommandToStatus(command);
        Status updatedStatus = statusService.updateStatusPersianDescription(status);
        return statusDataMapper.statusToUpdateResponse(updatedStatus, "Status persianDescription updated successfully");
    }
}

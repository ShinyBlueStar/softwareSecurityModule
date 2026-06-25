package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.model.Status;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.ports.output.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Map;


@Log4j2
@Validated
@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;

    @Override
    public Status findByCode(String code) {
        log.debug("StatusServiceImpl.findByCode started, code={}", code);
        return statusRepository.findByCode(code);
    }

    @Override
    @Transactional
    public Status createStatus(Status status) throws SsmDomainException {
        log.debug("StatusServiceImpl.createStatus started, code={}", status != null ? status.getCode() : null);
        if (status == null || status.getCode() == null) {
            throw new SsmDomainException("Status code is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        Status existingStatus = statusRepository.findByCode(status.getCode());
        if (existingStatus != null && existingStatus.getId() != null) {
            throw new SsmDomainException(
                    "Status with code " + status.getCode() + " already exists",
                    StatusService.STATUS_ALREADY_EXISTS,
                    HttpStatus.BAD_REQUEST);
        }

        Status savedStatus = statusRepository.save(status);
        if (savedStatus == null || savedStatus.getId() == null) {
            throw new SsmDomainException(
                    "Could not save status with code " + status.getCode(),
                    StatusService.STATUS_SAVE_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return savedStatus;
    }

    @Override
    @Transactional
    public Status updateStatusPersianDescription(Status status) throws SsmDomainException {
        log.debug("StatusServiceImpl.updateStatusPersianDescription started, code={}", status != null ? status.getCode() : null);
        if (status == null || status.getCode() == null) {
            throw new SsmDomainException("Status code is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        if (status.getPersianDescription() == null) {
            throw new SsmDomainException("Persian description is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        Status existingStatus = statusRepository.findByCode(status.getCode());
        if (existingStatus == null || existingStatus.getId() == null) {
            throw new SsmDomainException("Status not found with code: " + status.getCode(),
                    StatusService.ID_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        existingStatus.setPersianDescription(status.getPersianDescription());
        Status savedStatus = statusRepository.save(existingStatus);
        if (savedStatus == null || savedStatus.getId() == null) {
            throw new SsmDomainException(
                    "Could not update status with code " + status.getCode(),
                    StatusService.STATUS_UPDATE_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return savedStatus;
    }

    @Override
    public Page<Status> listStatuses(Map<String, String> params, String caller, String ip) throws SsmDomainException {
        log.debug("StatusServiceImpl.listStatuses started");
        log.info("Starting status list retrieval service");
        try {
            Page<Status> allStatuses = statusRepository.findAllStatuses(params);
            return allStatuses;
        } catch (Exception e) {
            log.error("Error occurred in status list retrieval service - error: {}", e.getMessage(), e);
            throw new SsmDomainException(
                    "Error occurred in status list retrieval service",
                    StatusService.STATUS_LIST_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

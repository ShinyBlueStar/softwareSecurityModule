package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.model.Status;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Status Application Service Interface
 * Defines operations for Status management
 * Following Hexagonal Architecture - Input Port
 */
public interface StatusService {

    int GENERAL_ERROR = 999;
    // PIN errors
    int PIN_GENERATION_FAILED = 1001;
    int PIN_VERIFICATION_FAILED = 1002;

    // OTP errors
    int OTP_GENERATION_FAILED = 1003;
    int OTP_VERIFICATION_FAILED = 1004;

    // CVV2 errors
    int CVV2_GENERATION_FAILED = 1005;
    int CVV2_VERIFICATION_FAILED = 1006;

    // Vault / third-party integration
    int VAULT_INVALID_RESPONSE = 1007;
    int VAULT_UNEXPECTED_ERROR = 1008;

    // Session errors
    int ACTIVE_SESSION_EXISTS = 1009;

    // Status management errors
    int STATUS_ALREADY_EXISTS = 1010;
    int STATUS_SAVE_FAILED = 1011;
    int STATUS_UPDATE_FAILED = 1012;
    int STATUS_LIST_FAILED = 1013;
    int ID_NOT_FOUND = 1014;
    int USER_HAS_NOT_PERMISSION = 1015;
    int SESSION_NOT_VALID = 1016;
    int INPUT_PARAMETER_NOT_VALID = 1017;
    int RATE_LIMIT_EXCEEDED = 1018;
    int ACTIVE_OTP_EXIST = 1019;
    int REQUESTED_CVV2_NOT_EXISTED = 1020;
    int CARD_IS_LOCKED = 1021;

    Status findByCode(String code);

    Status createStatus(@Valid Status status) throws SsmDomainException;

    Status updateStatusPersianDescription(@Valid Status status) throws SsmDomainException;

    Page<Status> listStatuses(Map<String, String> map, String caller, String ip) throws SsmDomainException;

}

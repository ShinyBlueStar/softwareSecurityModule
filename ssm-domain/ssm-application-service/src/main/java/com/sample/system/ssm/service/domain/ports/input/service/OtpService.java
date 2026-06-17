package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;

/**
 * OTP (PIN2) service - extends SecurityServiceProcessor for Strategy pattern.
 * cardData can be sessionId or PAN depending on usage.
 */
public interface OtpService extends SecurityServiceProcessor {

    Card generate(Card card) throws SsmDomainException;

    Boolean verify(Card card) throws SsmDomainException;
}


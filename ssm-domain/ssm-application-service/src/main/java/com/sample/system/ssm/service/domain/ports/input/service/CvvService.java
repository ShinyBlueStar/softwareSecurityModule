package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;

public interface CvvService extends SecurityServiceProcessor{
    Boolean verify(Card card) throws SsmDomainException;
    Card generate(Card card) throws SsmDomainException;
}


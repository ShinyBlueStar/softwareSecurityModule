package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;

public interface SecurityServiceProcessor {
    Boolean verify(Card cardData) throws SsmDomainException;
    Card generate(Card cardData) throws SsmDomainException;
}

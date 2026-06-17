package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.command.TransactionContext;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;

import java.util.UUID;

import com.sample.system.ssm.service.domain.response.UnblockCardResponse;

public interface CardService {
    Card generate(Card card) throws SsmDomainException;
    Card verify(Card card) throws SsmDomainException;
    String viewSecret(UUID sessionId, UUID cardId, SecretType secretType) throws SsmDomainException;
    UnblockCardResponse unblockCard(UUID sessionId) throws SsmDomainException;
}

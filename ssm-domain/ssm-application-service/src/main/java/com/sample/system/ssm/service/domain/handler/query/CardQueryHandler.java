package com.sample.system.ssm.service.domain.handler.query;

import com.sample.system.ssm.service.domain.command.ViewCardSecretCommand;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.ports.input.service.CardService;
import com.sample.system.ssm.service.domain.response.ViewCardSecretResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardQueryHandler {
    private final CardService cardService;

    public ViewCardSecretResponse viewSecret(UUID sessionId, ViewCardSecretCommand command) throws SsmDomainException {
        String encryptedSecret = cardService.viewSecret(sessionId, command.cardId(), command.secretType());
        return new ViewCardSecretResponse(encryptedSecret);
    }
}

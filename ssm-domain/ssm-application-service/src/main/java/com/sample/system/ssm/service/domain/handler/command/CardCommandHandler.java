package com.sample.system.ssm.service.domain.handler.command;

import com.sample.system.ssm.service.domain.command.GenerateRequestCommand;
import com.sample.system.ssm.service.domain.command.ValidateRequestCommand;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.mapper.CardDataMapper;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.ports.input.service.CardService;
import com.sample.system.ssm.service.domain.response.GenerateResponse;
import com.sample.system.ssm.service.domain.response.UnblockCardResponse;
import com.sample.system.ssm.service.domain.response.ValidateResponse;
import com.sample.system.ssm.service.domain.response.ValidateSecretResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardCommandHandler {
    private final CardService cardService;

    /** Session ID must come from X-Session-Id header (controller passes it here). */
    public GenerateResponse generate(UUID sessionId, GenerateRequestCommand requestCommand) throws SsmDomainException {
        log.info("[generate] sessionId={}, type={}", sessionId, requestCommand.getType());
        Card card = CardDataMapper.toModel(requestCommand, sessionId);
        return CardDataMapper.toGenerateResponse(cardService.generate(card));
    }

    public ValidateResponse verify(UUID sessionId, ValidateRequestCommand req) throws SsmDomainException {
        log.info("[verify] sessionId={}, type={}", sessionId, req.getType());
        Card card = cardService.verify(CardDataMapper.toModel(req, sessionId));
        return CardDataMapper.toVerifyResponse(card);
    }

    /** آنبلاک کارت: اعتبار سشن، استخراج کارت‌آیدی از سشن، در صورت بلاک بودن رفع بلاک. */
    public UnblockCardResponse unblockCard(UUID sessionId) throws SsmDomainException {
        log.info("[unblock] sessionId={}", sessionId);
        return cardService.unblockCard(sessionId);
    }
}

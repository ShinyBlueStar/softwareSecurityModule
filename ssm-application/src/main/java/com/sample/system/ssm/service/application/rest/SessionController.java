package com.sample.system.ssm.service.application.rest;

import com.sample.system.ssm.service.domain.command.CreateSessionCommand;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.handler.command.SessionCommandHandler;
import com.sample.system.ssm.service.domain.handler.query.SessionQueryHandler;
import com.sample.system.ssm.service.domain.response.CreateSessionResponse;
import com.sample.system.ssm.service.domain.response.InvalidateSessionResponse;
import com.sample.system.ssm.service.domain.response.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping(value = "/api/v1/ssm", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
public class SessionController {

    private final SessionQueryHandler sessionQueryHandler;
    private final SessionCommandHandler sessionCommandHandler;

    @PostMapping("/sessions")
    @Operation(summary = "Create session", description = "Create a new SSM session for card operations. channel: WEB|MOBILE|API|POS|ATM. sessionType: e.g. CARD_ACTIVATION, PIN_CHANGE, TRANSACTION. clientFingerprint: optional device/browser fingerprint for audit.")
    public ResponseEntity<BaseResponse<CreateSessionResponse>> createSession(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Session creation payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create session",
                                    value = """
                                            {
                                              "cardId": "550e8400-e29b-41d4-a716-446655440000",
                                              "channel": "WEB",
                                              "sessionType": "CARD_ACTIVATION",
                                              "clientFingerprint": "fp_a1b2c3d4e5f6_hash_xyz"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid CreateSessionCommand req) throws SsmDomainException {
        log.debug("SessionController.createSession started, cardId={}", req != null ? req.cardId() : null);
        var response = sessionCommandHandler.createSession(req);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @PostMapping("/sessions/{sessionId}/invalidate")
    public ResponseEntity<BaseResponse<Void>> invalidate(@PathVariable UUID sessionId) {
        log.debug("SessionController.invalidate started, sessionId={}", sessionId);
        sessionQueryHandler.invalidate(sessionId);
        return ResponseEntity.noContent().build();
    }
}

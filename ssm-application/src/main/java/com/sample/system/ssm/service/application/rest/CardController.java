package com.sample.system.ssm.service.application.rest;

import com.sample.system.ssm.service.domain.command.*;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.handler.command.CardCommandHandler;
import com.sample.system.ssm.service.domain.handler.query.CardQueryHandler;
import com.sample.system.ssm.service.domain.response.*;
import com.sample.system.ssm.service.domain.response.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping(value = "/api/v1/ssm/card", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardQueryHandler cardQueryHandler;
    private final CardCommandHandler cardCommandHandler;

    // ===================== GENERATE =====================

    @PostMapping("/generate")
    @Operation(
            security = {@SecurityRequirement(name = "bearerAuth")},
            summary = "Generate PIN1, CVV2, OTP",
            description = "Generate security data. PIN1 is stored in DB (encrypted+hash); CVV2 and OTP are not stored (PCI / one-time use). Session ID is taken from X-Session-Id header only (not in body).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "PIN/OTP: type only. CVV: type + cardNumber (16 digits) + expTime (4 digits MMyy) + serviceCode (3 digits). Session ID comes from header.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Generate PIN1",
                                            value = """
                                                    {
                                                      "type": "PIN"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Generate CVV2",
                                            value = """
                                                    {
                                                      "type": "CVV",
                                                      "encryptedData": "base64-encrypted-pan-expTime-serviceCode"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Generate OTP",
                                            value = """
                                                    {
                                                      "type": "OTP"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<BaseResponse<GenerateResponse>> generate(
            @Parameter(
                    description = "Session ID (required; do not repeat in body)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-Session-Id") String sessionId,

            @RequestBody @Valid GenerateRequestCommand requestCommand
    ) throws SsmDomainException {
        log.info("CardController.generate requestBody={}, sessionId={}", requestCommand, sessionId);

        GenerateResponse generateResponse =
                cardCommandHandler.generate(UUID.fromString(sessionId), requestCommand);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new BaseResponse<>(true, generateResponse));
    }

    // ===================== VALIDATE =====================

    @PostMapping("/validate")
    @Operation(
            summary = "Verify PIN/OTP/CVV",
            description = "Verify the supplied value against generated or stored secret. All 'value' fields are in encrypted format. For CVV validation, value is the combination of pan, expTime and serviceCode in encrypted format. Session ID is taken from X-Session-Id header only (not in body).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Body: type, cardNumber, value (encrypted). For CVV: value = encrypted(pan + expTime + serviceCode). Session ID comes from header.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Validate PIN",
                                            value = """
                                                    {
                                                      "type": "PIN",
                                                      "value": "base64-encrypted-pin-data"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Validate OTP",
                                            value = """
                                                    {
                                                      "type": "OTP",
                                                      "value": "base64-encrypted-otp-data"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Validate CVV",
                                            value = """
                                                    {
                                                      "type": "CVV",
                                                      "value": "base64-encrypted-pan-expTime-serviceCode"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<BaseResponse<ValidateResponse>> validate(
            @Parameter(
                    description = "Session ID (required; do not repeat in body)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-Session-Id") String sessionId,

            @RequestBody @Valid ValidateRequestCommand req
    ) throws SsmDomainException {

        ValidateResponse validateResponse =
                cardCommandHandler.verify(UUID.fromString(sessionId), req);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new BaseResponse<>(true, validateResponse));
    }

    // ===================== VIEW SECRET =====================

    @GetMapping("/secrets/view")
    @Operation(
            security = {@SecurityRequirement(name = "bearerAuth")},
            summary = "View Card Secret (PIN1)",
            description = "View encrypted PIN1 with rate limiting. CVV2 is not stored and cannot be viewed (PCI). Session ID is taken from X-Session-Id header only (not in body).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Body: secretType only (PIN1). Optional cardId. Session ID comes from header.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "View PIN1 (default)",
                                            value = """
                                                    {
                                                      "secretType": "PIN"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "View PIN1 with cardId",
                                            value = """
                                                    {
                                                      "secretType": "PIN",
                                                      "cardId": "12345"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<BaseResponse<ViewCardSecretResponse>> viewSecret(
            @Parameter(
                    description = "Session ID (required; do not repeat in body)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-Session-Id") String sessionId,

            @RequestBody @Valid ViewCardSecretCommand command
    ) throws SsmDomainException {
        log.info("CardController.viewSecret requestBody={}, sessionId={}", command, sessionId);

        ViewCardSecretResponse response =
                cardQueryHandler.viewSecret(UUID.fromString(sessionId), command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new BaseResponse<>(true, response));
    }

    // ===================== UNBLOCK CARD =====================

    @PostMapping("/unblock")
    @Operation(
            security = {@SecurityRequirement(name = "bearerAuth")},
            summary = "Unblock card",
            description = "Validates session, gets card ID from session, and if the card is blocked, releases the lock. Session ID from X-Session-Id header only. No request body."
    )
    public ResponseEntity<BaseResponse<UnblockCardResponse>> unblockCard(
            @Parameter(
                    description = "Session ID (required)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-Session-Id") String sessionId
    ) throws SsmDomainException {
        log.debug("CardController.unblockCard started, sessionId={}", sessionId);

        UnblockCardResponse response = cardCommandHandler.unblockCard(UUID.fromString(sessionId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new BaseResponse<>(true, response));
    }
}

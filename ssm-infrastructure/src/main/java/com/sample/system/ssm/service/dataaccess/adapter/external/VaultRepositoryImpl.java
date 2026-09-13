package com.sample.system.ssm.service.dataaccess.adapter.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.ports.output.repository.external.VaultRepository;
import com.sample.system.ssm.service.thirdparty.ThirdPartyException;
import com.sample.system.ssm.service.thirdparty.WebCallUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of VaultRepository using WebCallUtils for REST communication with vault-service
 * Following Hexagonal Architecture - Output Adapter
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Primary
@Profile("!test & !local & !mock-vault")
public class VaultRepositoryImpl implements VaultRepository {

    private final ObjectMapper objectMapper;

    @Value("${vault.base.url}")
    private String vaultBaseUrl;

    @Value("${vault.timeout-seconds:5}")
    private int timeoutSeconds;

    private static final int[] SUCCESS_HTTP_STATUSES = {200, 201};

    @CircuitBreaker(name = "vaultService")
    @Retry(name = "vaultService")
    @Override
    public PinGenerationResult generatePin(Card card) throws SsmDomainException {
        String methodName = "VaultRepositoryImpl.generatePin";
        log.debug("{} started, cardId={}", methodName, card != null ? card.getCardId() : null);
        log.debug("Calling vault-service to generate PIN for CardId:{}", card.getCardId());
        try {
            var request = new GeneratePinRequest(card.getCardId().toString());
            String requestBody = objectMapper.writeValueAsString(request);

            String responseBody = callVaultService(
                    methodName,
                    HttpMethod.POST,
                    "/api/v1/vault/pin/generate",
                    requestBody
            );

            VaultPinResponse response = objectMapper.readValue(responseBody, VaultPinResponse.class);

            if (response.data() == null) {
                throw new SsmDomainException(
                        "Invalid response from vault-service: data is null",
                        StatusService.VAULT_INVALID_RESPONSE,
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            log.debug("PIN generated successfully");
            return new PinGenerationResult(response.data().encryptedPin(), response.data().hashPin());

        } catch (ThirdPartyException e) {
            log.error("ThirdPartyException in {}: httpStatus={}, message={}", methodName, e.getHttpStatusCode(), e.getMessage());
            throw convertToSsmDomainException(e, "Failed to generate PIN from vault-service");
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            throw new SsmDomainException(
                    "Unexpected error generating PIN: " + e.getMessage(),
                    StatusService.VAULT_UNEXPECTED_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CircuitBreaker(name = "vaultService")
    @Retry(name = "vaultService")
    @Override
    public boolean verifyPin(Card card) throws SsmDomainException {
        String methodName = "VaultRepositoryImpl.verifyPin";
        log.debug("{} started, cardId={}", methodName, card != null ? card.getCardId() : null);
        log.debug("Calling vault-service to verify PIN for CardId: {}", card.getCardId());
        try {
            var request = new VerifyPinRequest(card.getCardId().toString(),
                    card.getHashedPin(), card.getEncryptedPin());
            String requestBody = objectMapper.writeValueAsString(request);
            String responseBody = callVaultService(
                    methodName,
                    HttpMethod.POST,
                    "/api/v1/vault/pin/verify",
                    requestBody
            );
            VaultVerifyResponse response = objectMapper.readValue(responseBody, VaultVerifyResponse.class);
            boolean isValid = response.data() != null && response.data().valid();

            log.debug("PIN verification result: {}", isValid);
            return isValid;
        } catch (ThirdPartyException e) {
            log.error("ThirdPartyException in {}: httpStatus={}, message={}", methodName, e.getHttpStatusCode(), e.getMessage());
            throw convertToSsmDomainException(e, "Failed to verify PIN from vault-service");
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            throw new SsmDomainException(
                    "Unexpected error verifying PIN: " + e.getMessage(),
                    StatusService.VAULT_UNEXPECTED_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CircuitBreaker(name = "vaultService")
    @Retry(name = "vaultService")
    @Override
    public OtpGenerationResult generateOtp(Card card) throws SsmDomainException {
        String methodName = "VaultRepositoryImpl.generateOtp";
        log.debug("{} started, cardId={}", methodName, card != null ? card.getCardId() : null);
        log.debug("Calling vault-service to generate OTP for CardId: {}", card.getCardId());

        try {
            var request = new GenerateOtpRequest(card.getCardId().toString());
            String requestBody = objectMapper.writeValueAsString(request);

            String responseBody = callVaultService(
                    methodName,
                    HttpMethod.POST,
                    "/api/v1/vault/otp/generate",
                    requestBody
            );

            VaultOtpResponse response = objectMapper.readValue(responseBody, VaultOtpResponse.class);

            if (response.data() == null
                    || response.data().encryptedOtp() == null
                    || response.data().hashOtp() == null
                    || response.data().expireAt() == null) {
                throw new SsmDomainException(
                        "Invalid response from vault-service: OTP data is incomplete",
                        StatusService.VAULT_INVALID_RESPONSE,
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            log.debug("OTP generated successfully");
            return new OtpGenerationResult(
                    response.data().encryptedOtp(),
                    response.data().hashOtp(),
                    response.data().expireAt()
            );
        } catch (ThirdPartyException e) {
            log.error("ThirdPartyException in {}: httpStatus={}, message={}",
                    methodName, e.getHttpStatusCode(), e.getMessage());
            throw convertToSsmDomainException(e, "Failed to generate OTP from vault-service");
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            throw new SsmDomainException(
                    "Unexpected error generating OTP: " + e.getMessage(),
                    StatusService.VAULT_UNEXPECTED_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CircuitBreaker(name = "vaultService")
    @Retry(name = "vaultService")
    @Override
    public boolean verifyOtp(Card card) throws SsmDomainException {
        String methodName = "VaultRepositoryImpl.verifyOtp";
        log.debug("{} started, cardId={}", methodName, card != null ? card.getCardId() : null);
        log.debug("Calling vault-service to verify OTP for CardId: {}", card.getCardId());

        try {
            var request = new VerifyOtpRequest(card.getCardId().toString(), card.getEncryptedOtp());
            String requestBody = objectMapper.writeValueAsString(request);

            String responseBody = callVaultService(
                    methodName,
                    HttpMethod.POST,
                    "/api/v1/vault/otp/verify",
                    requestBody
            );

            VaultVerifyResponse response = objectMapper.readValue(responseBody, VaultVerifyResponse.class);
            boolean isValid = response.data() != null && response.data().valid();

            log.debug("OTP verification result: {}", isValid);
            return isValid;

        } catch (ThirdPartyException e) {
            log.error("ThirdPartyException in {}: httpStatus={}, message={}", methodName, e.getHttpStatusCode(), e.getMessage());
            throw convertToSsmDomainException(e, "Failed to verify OTP from vault-service");
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            throw new SsmDomainException(
                    "Unexpected error verifying OTP: " + e.getMessage(),
                    StatusService.VAULT_UNEXPECTED_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CircuitBreaker(name = "vaultService")
    @Retry(name = "vaultService")
    @Override
    public CvvGenerationResult generateCvv2(Card card) throws SsmDomainException {
        String methodName = "VaultRepositoryImpl.generateCvv2";
        log.info("{} started, cardId={}", methodName, card != null ? card.getCardId() : null);
        log.info("Calling vault-service to generate CVV2 for CardId: {}",card.getCardId());

        try {
            var request = new GenerateCvvRequest(card.getCardId().toString(), card.getEncryptedData());
            String requestBody = objectMapper.writeValueAsString(request);
            String responseBody = callVaultService(
                    methodName,
                    HttpMethod.POST,
                    "/api/v1/vault/cvv/generate",
                    requestBody
            );

            VaultCvvResponse response = objectMapper.readValue(responseBody, VaultCvvResponse.class);

            if (response.data() == null || response.data().encryptedCvv() == null) {
                throw new SsmDomainException(
                        "Invalid response from vault-service: CVV2 is null",
                        StatusService.VAULT_INVALID_RESPONSE,
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
            log.debug("CVV2 generated successfully");
            return new CvvGenerationResult(response.data().encryptedCvv(), response.data().hashCvv());
        } catch (ThirdPartyException e) {
            log.error("ThirdPartyException in {}: httpStatus={}, message={}", methodName,
                    e.getHttpStatusCode(), e.getMessage());
            throw convertToSsmDomainException(e, "Failed to generate CVV2 from vault-service");
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            throw new SsmDomainException(
                    "Unexpected error generating CVV2: " + e.getMessage(),
                    StatusService.VAULT_UNEXPECTED_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CircuitBreaker(name = "vaultService")
    @Retry(name = "vaultService")
    @Override
    public boolean verifyCvv2(Card card) throws SsmDomainException {
        String methodName = "VaultRepositoryImpl.verifyCvv2";
        log.info("{} started, cardId={}", methodName, card != null ? card.getCardId() : null);

        if (card == null || card.getCardId() == null || card.getHashedCvv() == null || card.getValue() == null || card.getValue().isBlank()) {
            throw new SsmDomainException(
                    "CVV verify requires cardId, hashedCvv (from DB) and value (encrypted data from client)",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            var request = new VerifyCvvRequest(
                    card.getCardId().toString(),
                    card.getHashedCvv(),
                    card.getValue()
            );
            String requestBody = objectMapper.writeValueAsString(request);
            log.info("Sending CVV verify to vault for cardId: {} (encrypted value forwarded from client)", card.getCardId());

            String responseBody = callVaultService(
                    methodName,
                    HttpMethod.POST,
                    "/api/v1/vault/cvv/verify",
                    requestBody
            );

            VaultVerifyResponse response = objectMapper.readValue(responseBody, VaultVerifyResponse.class);
            boolean isValid = response.data() != null && response.data().valid();

            log.info("CVV2 verification result: {}", isValid);
            return isValid;

        } catch (ThirdPartyException e) {
            log.error("ThirdPartyException in {}: httpStatus={}, message={}", methodName, e.getHttpStatusCode(), e.getMessage());
            throw convertToSsmDomainException(e, "Failed to verify CVV2 from vault-service");
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            throw new SsmDomainException(
                    "Unexpected error verifying CVV2: " + e.getMessage(),
                    StatusService.VAULT_UNEXPECTED_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String callVaultService(String methodName, HttpMethod httpMethod, String endpoint, String requestBody)
            throws ThirdPartyException {
        log.debug("VaultRepositoryImpl.callVaultService started, method={}, endpoint={}", methodName, endpoint);
        try {
            URIBuilder uriBuilder = new URIBuilder(vaultBaseUrl + endpoint);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/vnd.api.v1+json");

            Map<String, String> params = new HashMap<>();

            StringEntity entity = requestBody != null
                    ? new StringEntity(requestBody, StandardCharsets.UTF_8)
                    : null;

            int timeoutMillis = timeoutSeconds * 1000;

            return WebCallUtils.sendRequest(
                    methodName,
                    httpMethod,
                    uriBuilder,
                    timeoutMillis,
                    headers,
                    params,
                    entity,
                    SUCCESS_HTTP_STATUSES
            );

        } catch (Exception e) {
            log.error("Error building request in {}: {}", methodName, e.getMessage(), e);
            throw new ThirdPartyException(
                    "Error building request: " + e.getMessage(),
                    WebCallUtils.GENERAL_ERROR,
                    e.getMessage(),
                    -1,
                    ""
            );
        }
    }

    /**
     * Convert ThirdPartyException to SsmDomainException
     */
    private SsmDomainException convertToSsmDomainException(ThirdPartyException e, String context) {
        log.debug("VaultRepositoryImpl.convertToSsmDomainException started, context={}", context);
        int httpStatusCode = e.getHttpStatusCode() > 0 ? e.getHttpStatusCode() : 500;
        HttpStatus httpStatus = HttpStatus.resolve(httpStatusCode);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String message = context + ": " + e.getChannelMessage();
        if (e.getCompleteResponse() != null && !e.getCompleteResponse().isEmpty()) {
            message += " - Response: " + e.getCompleteResponse();
        }

        return new SsmDomainException(
                message,
                StatusService.VAULT_UNEXPECTED_ERROR,
                httpStatus
        );
    }

    // DTOs for Vault Service Communication
    private record GeneratePinRequest(String cardId) {}
    private record VerifyPinRequest(String cardId, String hashedPin, String providedPin) {}
    private record GenerateOtpRequest(String cardId) {}
    private record VerifyOtpRequest(String cardId, String providedOtp) {}
    private record GenerateCvvRequest(String cardId, String encryptedData) {}
    private record VerifyCvvRequest(String cardId, String hashedCvv, String encryptedData) {}

    // Response DTOs matching vault-service BaseResponse structure
    private record VaultPinData(String encryptedPin, String hashPin) {}
    private record VaultOtpData(String encryptedOtp, String hashOtp, Instant expireAt) {}
    private record VaultCvvData(String encryptedCvv, String hashCvv) {}
    private record VaultVerifyData(boolean valid) {}

    // Response wrappers matching vault-service response format
    private record VaultPinResponse(boolean success, VaultPinData data) {}
    private record VaultOtpResponse(boolean success, VaultOtpData data) {}
    private record VaultCvvResponse(boolean success, VaultCvvData data) {}
    private record VaultVerifyResponse(boolean success, VaultVerifyData data) {}
}

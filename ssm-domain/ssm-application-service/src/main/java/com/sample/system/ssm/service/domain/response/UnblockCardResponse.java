package com.sample.system.ssm.service.domain.response;

import java.util.UUID;

/**
 * Response for unblock card operation.
 */
public record UnblockCardResponse(
        UUID cardId,
        boolean unblocked,
        String message
) {}

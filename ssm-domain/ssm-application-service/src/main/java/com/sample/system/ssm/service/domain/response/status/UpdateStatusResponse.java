package com.sample.system.ssm.service.domain.response.status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateStatusResponse {
    private final Long statusId;
    private final String code;
    private final String persianDescription;
    private final String message;
}

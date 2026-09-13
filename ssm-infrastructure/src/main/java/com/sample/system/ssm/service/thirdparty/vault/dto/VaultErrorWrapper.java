package com.sample.system.ssm.service.thirdparty.vault.dto;

import com.sample.system.ssm.service.domain.response.base.ErrorDetail;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VaultErrorWrapper {
    private ErrorDetail error;
    private Object response;
}
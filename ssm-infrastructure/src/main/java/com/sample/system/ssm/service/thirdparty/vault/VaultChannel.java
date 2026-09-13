package com.sample.system.ssm.service.thirdparty.vault;

import com.sample.system.ssm.service.thirdparty.ThirdPartyException;
import com.sample.system.ssm.service.thirdparty.vault.dto.ValidateRequestDto;

public interface VaultChannel {
    public String GENERAL_ERROR = "999";
    public String TIMEOUT = "998";

    String generate(String sessionIdn ) throws ThirdPartyException;

    String validate(ValidateRequestDto req) throws ThirdPartyException;
}

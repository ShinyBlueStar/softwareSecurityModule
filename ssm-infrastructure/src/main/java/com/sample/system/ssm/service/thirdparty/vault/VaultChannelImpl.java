package com.sample.system.ssm.service.thirdparty.vault;

import com.sample.system.ssm.service.thirdparty.ThirdPartyException;
import com.sample.system.ssm.service.thirdparty.vault.dto.ValidateRequestDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Profile({"development"})
public class VaultChannelImpl implements VaultChannel {

    @Value("${vault.timeout-seconds}")
    private String timeout;

    @Value("${vault.base.url}")
    private String baseUrl;


    @Override
    public String generate(String sessionIdn) throws ThirdPartyException {
        log.debug("VaultChannelImpl.generate started, sessionIdn={}", sessionIdn);
        return "";
    }

    @Override
    public String validate(ValidateRequestDto req) throws ThirdPartyException {
        log.debug("VaultChannelImpl.validate started");
        return "";
    }

}

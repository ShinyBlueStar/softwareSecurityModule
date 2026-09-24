package com.sample.system.ssm.service;

import com.sample.system.ssm.service.domain.SsmDomainServiceImpl;
import com.sample.system.ssm.service.domain.SsmDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BeanConfiguration {

    @Bean
    public SsmDomainService ssmDomainService() {
        return new SsmDomainServiceImpl();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
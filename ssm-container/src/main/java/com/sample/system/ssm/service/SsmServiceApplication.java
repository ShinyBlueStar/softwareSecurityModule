package com.sample.system.ssm.service;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories(basePackages = { "com.sample.system.ssm.service.dataaccess.repository"})
@EntityScan(basePackages = { "com.sample.system.ssm.service.dataaccess.entity" })
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.sample.system.ssm",
        "com.sample.system.ssm.service.dataaccess.adapter",
        "com.sample.system.ssm.service.dataaccess.mapper",
        "com.sample.system.ssm.service.application.rest"
})
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "5m")
public class SsmServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SsmServiceApplication.class, args);
    }
}

package com.sample.system.ssm.service.domain.job;

import com.sample.system.ssm.service.domain.ports.output.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireOtpsJob {

    private final OtpRepository otpRepository;

    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(
            name = "expireOtpsJob",
            lockAtLeastFor = "50s",
            lockAtMostFor = "3m"
    )
    public void expireOtps() {
        log.debug("ExpireOtpsJob.expireOtps started");
        Instant now = Instant.now();
        int count = otpRepository.markExpiredOtps(now);
        if (count > 0) {
            log.info("ExpireOtpsJob: marked {} OTP(s) as EXPIRED", count);
        }
    }
}

package com.sample.system.ssm.service.domain.job;

import com.sample.system.ssm.service.domain.ports.output.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireSessionsJob {

    private final SessionRepository sessionRepository;

    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(
            name = "expireSessionsJob",
            lockAtLeastFor = "50s",
            lockAtMostFor = "3m"
    )
    public void expireSessions() {
        log.debug("ExpireSessionsJob.expireSessions started");
        Instant now = Instant.now();
        int count = sessionRepository.markExpiredSessions(now);
        if (count > 0) {
            log.info("ExpireSessionsJob: marked {} session(s) as EXPIRED", count);
        }
    }
}

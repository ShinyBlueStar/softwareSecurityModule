package com.sample.system.ssm.service.domain.job;

import com.sample.system.ssm.service.domain.ports.output.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteExpiredSessionsJob {

    private static final String JOB_NAME = "deleteExpiredSessionsJob";

    private final SessionRepository sessionRepository;

    /**
     * Runs every night at 02:00. Deletes all sessions with status EXPIRED.
     * Exceptions are caught and logged so the scheduler keeps running.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @SchedulerLock(
            name = JOB_NAME,
            lockAtLeastFor = "30s",
            lockAtMostFor = "5m"
    )
    public void deleteExpiredSessions() {
        log.debug("DeleteExpiredSessionsJob.deleteExpiredSessions started");
        log.info("DeleteExpiredSessionsJob: starting nightly cleanup of EXPIRED sessions");
        try {
            int deleted = sessionRepository.deleteExpiredSessions();
            if (deleted > 0) {
                log.info("DeleteExpiredSessionsJob: successfully deleted {} session(s) with status EXPIRED", deleted);
            } else {
                log.debug("DeleteExpiredSessionsJob: no EXPIRED sessions to delete");
            }
        } catch (Exception e) {
            log.error("DeleteExpiredSessionsJob: failed to delete EXPIRED sessions – error will not stop scheduler. Cause: {}", e.getMessage(), e);
            // Do not rethrow: keep the job from breaking the application and allow next run
        }
        log.info("DeleteExpiredSessionsJob: finished");
    }
}

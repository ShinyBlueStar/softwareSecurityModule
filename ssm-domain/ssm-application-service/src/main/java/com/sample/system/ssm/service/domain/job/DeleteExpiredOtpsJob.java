package com.sample.system.ssm.service.domain.job;

import com.sample.system.ssm.service.domain.ports.output.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteExpiredOtpsJob {

    private static final String JOB_NAME = "deleteExpiredOtpsJob";

    private final OtpRepository otpRepository;

    /**
     * Runs every night at 02:00. Deletes all OTPs with status EXPIRED or USED.
     * Exceptions are caught and logged so the scheduler keeps running.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @SchedulerLock(
            name = JOB_NAME,
            lockAtLeastFor = "30s",
            lockAtMostFor = "5m"
    )
    public void deleteExpiredOtps() {
        log.debug("DeleteExpiredOtpsJob.deleteExpiredOtps started");
        log.info("DeleteExpiredOtpsJob: starting nightly cleanup of EXPIRED and USED OTPs");
        try {
            int deleted = otpRepository.deleteExpiredOtps();
            if (deleted > 0) {
                log.info("DeleteExpiredOtpsJob: successfully deleted {} OTP(s) with status EXPIRED or USED", deleted);
            } else {
                log.debug("DeleteExpiredOtpsJob: no EXPIRED or USED OTPs to delete");
            }
        } catch (Exception e) {
            log.error("DeleteExpiredOtpsJob: failed to delete EXPIRED/USED OTPs – error will not stop scheduler. Cause: {}", e.getMessage(), e);
            // Do not rethrow: keep the job from breaking the application and allow next run
        }
        log.info("DeleteExpiredOtpsJob: finished");
    }
}

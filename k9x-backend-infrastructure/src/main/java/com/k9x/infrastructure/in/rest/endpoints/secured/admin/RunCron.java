package com.k9x.infrastructure.in.rest.endpoints.secured.admin;

import com.k9x.application.crons.use_case.RunCronServiceCase;
import com.k9x.application.crons.use_case.command.CronJob;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * {@code POST /secured/admin/crons?cron=snapshots|dog-rank-history}: runs that scheduled job by hand and answers
 * with how many rows it wrote. Under {@code /secured/} so the {@code Auth} filter demands a token; the service case
 * then rejects anybody but the support account. An unknown {@code cron} is a 400 that lists the valid ones.
 *
 * <p>Hand-written like {@code Refresh}: it is an operator tool, not part of the public contract, so it is not in
 * the OAS definition. It answers when the job ends; if the platform's proxy cuts the connection first, the job
 * still finishes and its result is in the logs.
 */
@RestController
public class RunCron {

    private final RunCronServiceCase runCronServiceCase;
    private final UserInfoDTO userDetails;

    public RunCron(RunCronServiceCase runCronServiceCase, UserInfoDTO userDetails) {
        this.runCronServiceCase = runCronServiceCase;
        this.userDetails = userDetails;
    }

    @PostMapping("/secured/admin/crons")
    public ResponseEntity<Map<String, Object>> runCron(@RequestParam("cron") String cron) {
        return CronJob.fromParam(cron)
                .<ResponseEntity<Map<String, Object>>>map(job -> ResponseEntity.ok(Map.of(
                        "cron", job.param(),
                        "written", runCronServiceCase.run(job, userDetails.getEmail()))))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of(
                        "error", "unknown cron '" + cron + "'",
                        "valid", Arrays.stream(CronJob.values()).map(CronJob::param).toList())));
    }
}

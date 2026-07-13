package com.xrdj.iris.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("importAnomalyJob")
    private Job importAnomalyJob;

    @Autowired
    @Qualifier("importRuleCounterJob")
    private Job importRuleCounterJob;

    @PostMapping("/run-anomalies")
    public ResponseEntity<String> runAnomalyBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(importAnomalyJob, jobParameters);
            return ResponseEntity.ok("Anomaly extraction batch job has been invoked.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to invoke batch job: " + e.getMessage());
        }
    }

    @PostMapping("/run-rules")
    public ResponseEntity<String> runRuleCounterBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(importRuleCounterJob, jobParameters);
            return ResponseEntity.ok("Rule Counter extraction batch job has been invoked.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to invoke batch job: " + e.getMessage());
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.xrdj.iris.repository.AnomalyRecordRepository anomalyRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.xrdj.iris.repository.RuleCounterRecordRepository ruleCounterRepository;

    @org.springframework.web.bind.annotation.GetMapping("/anomalies")
    public ResponseEntity<java.util.List<com.xrdj.iris.model.AnomalyRecord>> getAnomalies() {
        return ResponseEntity.ok(anomalyRepository.findAll());
    }

    @org.springframework.web.bind.annotation.GetMapping("/rules")
    public ResponseEntity<java.util.List<com.xrdj.iris.model.RuleCounterRecord>> getRules() {
        return ResponseEntity.ok(ruleCounterRepository.findAll());
    }
}

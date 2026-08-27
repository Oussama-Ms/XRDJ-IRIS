package com.xrdj.iris.service;

import com.xrdj.iris.model.AccountingTreatment;
import com.xrdj.iris.model.FileArchive;
import com.xrdj.iris.model.RuleCounterRecord;
import com.xrdj.iris.repository.FileArchiveRepository;
import com.xrdj.iris.repository.RuleCounterRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TreatmentService {

    private final RuleCounterRecordRepository ruleCounterRecordRepository;
    private final com.xrdj.iris.repository.AnomalyRecordRepository anomalyRecordRepository;
    private final FileArchiveRepository fileArchiveRepository;
    private final JobLauncher jobLauncher;
    private final Job importRuleCounterJob;
    private final Job importAnomalyJob;
    private final SimpMessagingTemplate messagingTemplate;

    public TreatmentService(
            RuleCounterRecordRepository ruleCounterRecordRepository,
            com.xrdj.iris.repository.AnomalyRecordRepository anomalyRecordRepository,
            FileArchiveRepository fileArchiveRepository,
            JobLauncher jobLauncher,
            @Qualifier("importRuleCounterJob") Job importRuleCounterJob,
            @Qualifier("importAnomalyJob") Job importAnomalyJob,
            SimpMessagingTemplate messagingTemplate) {
        this.ruleCounterRecordRepository = ruleCounterRecordRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.fileArchiveRepository = fileArchiveRepository;
        this.jobLauncher = jobLauncher;
        this.importRuleCounterJob = importRuleCounterJob;
        this.importAnomalyJob = importAnomalyJob;
        this.messagingTemplate = messagingTemplate;
    }

    public List<AccountingTreatment> getAllTreatments() {
        List<RuleCounterRecord> records = ruleCounterRecordRepository.findAll();

        return records.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private AccountingTreatment mapToDTO(RuleCounterRecord record) {
        Long creRecus = parseLongSafe(record.getCreRecus());
        Long creTraites = parseLongSafe(record.getCreTraites());
        Long creRejetes = parseLongSafe(record.getCreRejetes());
        Long meGeneres = parseLongSafe(record.getMeGeneres());

        String statut = "Traité complètement";
        if (creRejetes > 0 && creTraites > 0) {
            statut = "Rejeté partiellement";
        } else if (creTraites == 0 && creRecus > 0) {
            statut = "Rejeté complètement";
        } else if (creRejetes > 0) {
            statut = "Rejeté partiellement";
        }

        LocalDateTime dateTraitement = LocalDateTime.now();
        String fileName = record.getFileName();

        // Find the actual ingestion date from the archive
        if (fileName != null && !fileName.isEmpty()) {
            java.util.Optional<FileArchive> archiveOpt =
                    fileArchiveRepository.findByFileName(fileName);
            if (archiveOpt.isPresent()) {
                dateTraitement = archiveOpt.get().getIngestionDate();
            }
        }
        String typeFlux = "CRE_GL_AB_FLEXI.seq";
        String nomApplication =
                record.getRecordType() != null && !record.getRecordType().trim().isEmpty()
                        ? record.getRecordType().trim()
                        : "CRE";

        if (fileName != null && !fileName.isEmpty()) {
            if (fileName.startsWith("Rule_Counter_CRE_CRE_")) {
                typeFlux = fileName.substring("Rule_Counter_CRE_CRE_".length());
            } else if (fileName.startsWith("Rule_Counter_CRE_")) {
                typeFlux = fileName.substring("Rule_Counter_CRE_".length());
            } else if (fileName.startsWith("Rule_Counter_")) {
                typeFlux = fileName.substring("Rule_Counter_".length());
            } else {
                typeFlux = fileName;
            }
        } else {
            if (record.getRecordType() == null
                    && record.getAppCode1() != null
                    && !record.getAppCode1().trim().isEmpty()) {
                nomApplication = record.getAppCode1().trim();
            }
        }

        return AccountingTreatment.builder()
                .id(String.valueOf(record.getId()))
                .dateTraitement(dateTraitement)
                .nomApplication(nomApplication)
                .typeFlux(typeFlux)
                .nbCreRecus(creRecus)
                .nbCreTraites(creTraites)
                .nbCreRejetes(creRejetes)
                .nbMeGeneres(meGeneres)
                .statut(statut)
                .build();
    }

    private Long parseLongSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public void triggerManualIngestion() {
        java.io.File dataDir = new java.io.File("../data");
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            return;
        }

        java.io.File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".seq"));
        if (files == null) {
            return;
        }

        int successCount = 0;
        int failCount = 0;
        int skippedCount = 0;

        for (java.io.File file : files) {
            String fileName = file.getName();

            try {
                if (fileName.startsWith("Rule_Counter")) {
                    if (fileArchiveRepository.existsByFileName(fileName)) {
                        skippedCount++;
                        continue;
                    }
                    JobParameters jobParameters =
                            new JobParametersBuilder()
                                    .addLong("time", System.currentTimeMillis())
                                    .addString("filePath", file.getAbsolutePath())
                                    .toJobParameters();

                    jobLauncher.run(importRuleCounterJob, jobParameters);

                    fileArchiveRepository.save(
                            FileArchive.builder()
                                    .fileName(fileName)
                                    .fileType("RULE_COUNTER")
                                    .ingestionDate(LocalDateTime.now())
                                    .status("COMPLETED")
                                    .build());
                    successCount++;
                } else if (fileName.startsWith("Details_Anomaly_Rejected_CRE")) {
                    if (fileArchiveRepository.existsByFileName(fileName)) {
                        skippedCount++;
                        continue;
                    }
                    JobParameters jobParameters =
                            new JobParametersBuilder()
                                    .addLong("time", System.currentTimeMillis())
                                    .addString("filePath", file.getAbsolutePath())
                                    .toJobParameters();

                    jobLauncher.run(importAnomalyJob, jobParameters);

                    fileArchiveRepository.save(
                            FileArchive.builder()
                                    .fileName(fileName)
                                    .fileType("ANOMALY")
                                    .ingestionDate(LocalDateTime.now())
                                    .status("COMPLETED")
                                    .build());
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
                e.printStackTrace();
            }
        }

        // Smart Summary Notification
        if (successCount > 0 || failCount > 0) {
            String summary =
                    String.format(
                            "Ingestion complete. Processed: %d, Skipped: %d, Failed: %d",
                            successCount, skippedCount, failCount);
            messagingTemplate.convertAndSend("/topic/alerts", summary);
        } else if (skippedCount > 0) {
            messagingTemplate.convertAndSend(
                    "/topic/alerts",
                    "No new files to ingest. Skipped " + skippedCount + " existing files.");
        }
    }

    public com.xrdj.iris.dto.SystemHealthDto getSystemHealth() {
        int errorCount =
                (int)
                        getAllTreatments().stream()
                                .mapToLong(AccountingTreatment::getNbCreRejetes)
                                .sum();

        int pendingFiles = 0;
        java.io.File dataDir = new java.io.File("../data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            java.io.File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".seq"));
            if (files != null) {
                for (java.io.File file : files) {
                    if (!fileArchiveRepository.existsByFileName(file.getName())) {
                        pendingFiles++;
                    }
                }
            }
        }

        return new com.xrdj.iris.dto.SystemHealthDto(1, pendingFiles, errorCount);
    }

    public void clearAllData() {
        ruleCounterRecordRepository.deleteAll();
        anomalyRecordRepository.deleteAll();
        fileArchiveRepository.deleteAll();
    }
}

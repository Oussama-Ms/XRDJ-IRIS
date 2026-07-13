package com.xrdj.iris.service;

import com.xrdj.iris.model.AccountingTreatment;
import com.xrdj.iris.model.RuleCounterRecord;
import com.xrdj.iris.repository.RuleCounterRecordRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TreatmentService {

    private final RuleCounterRecordRepository ruleCounterRecordRepository;
    private final JobLauncher jobLauncher;
    private final Job importRuleCounterJob;

    public TreatmentService(RuleCounterRecordRepository ruleCounterRecordRepository,
                            JobLauncher jobLauncher,
                            @Qualifier("importRuleCounterJob") Job importRuleCounterJob) {
        this.ruleCounterRecordRepository = ruleCounterRecordRepository;
        this.jobLauncher = jobLauncher;
        this.importRuleCounterJob = importRuleCounterJob;
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
        // The dateStart field now contains the 14-character date from the file header (e.g., 20260520145031)
        if (record.getDateStart() != null && record.getDateStart().length() == 14) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                dateTraitement = LocalDateTime.parse(record.getDateStart(), formatter);
            } catch (DateTimeParseException e) {
                // Keep the fallback
            }
        }

        String fileName = record.getFileName();
        String typeFlux = "CRE_GL_AB_FLEXI.seq";
        String nomApplication = "CRE";

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
            
            nomApplication = "CRE";
        } else {
            if (record.getAppCode1() != null && !record.getAppCode1().trim().isEmpty()) {
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

        java.io.File[] files = dataDir.listFiles((dir, name) -> name.startsWith("Rule_Counter") && name.endsWith(".seq"));
        if (files == null) {
            return;
        }

        for (java.io.File file : files) {
            String fileName = file.getName();
            
            // Skip already processed files
            if (ruleCounterRecordRepository.existsByFileName(fileName)) {
                continue;
            }

            try {
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .addString("filePath", file.getAbsolutePath())
                        .toJobParameters();
                jobLauncher.run(importRuleCounterJob, jobParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public com.xrdj.iris.dto.SystemHealthDto getSystemHealth() {
        int errorCount = (int) getAllTreatments().stream()
                .mapToLong(AccountingTreatment::getNbCreRejetes)
                .sum();
        
        int pendingFiles = 0;
        java.io.File dataDir = new java.io.File("../data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            java.io.File[] files = dataDir.listFiles((dir, name) -> name.startsWith("Rule_Counter") && name.endsWith(".seq"));
            if (files != null) {
                for (java.io.File file : files) {
                    if (!ruleCounterRecordRepository.existsByFileName(file.getName())) {
                        pendingFiles++;
                    }
                }
            }
        }
        
        return new com.xrdj.iris.dto.SystemHealthDto(1, pendingFiles, errorCount);
    }

    public void clearAllData() {
        ruleCounterRecordRepository.deleteAll();
    }

    public void reprocessTreatment(String id) {
        // Not implemented for DB right now
    }
}

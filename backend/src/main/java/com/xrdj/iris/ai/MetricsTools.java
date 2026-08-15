package com.xrdj.iris.ai;

import com.xrdj.iris.model.FileArchive;
import com.xrdj.iris.model.RuleCounterRecord;
import com.xrdj.iris.repository.FileArchiveRepository;
import com.xrdj.iris.repository.RuleCounterRecordRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Slf4j
@Configuration
public class MetricsTools {

    private final RuleCounterRecordRepository repository;
    private final FileArchiveRepository archiveRepository;

    public MetricsTools(
            RuleCounterRecordRepository repository, FileArchiveRepository archiveRepository) {
        this.repository = repository;
        this.archiveRepository = archiveRepository;
    }

    public record MetricsRequest(String flowType) {}

    public record MetricsResponse(int totalRejectedCREs) {}

    @Bean
    @Description(
            "Gets the total number of rejected CREs (Compte Rendu d'Evénement) treated TODAY in the"
                    + " database for a given banking flow type (e.g., 'Dotation', 'Virement').")
    public Function<MetricsRequest, MetricsResponse> getRejectedCREsToday() {
        return request -> {
            log.info("Function Called: getRejectedCREsToday for flow: {}", request.flowType());

            // Get all files ingested TODAY
            LocalDate today = LocalDate.now();
            List<String> filesToday =
                    archiveRepository.findAll().stream()
                            .filter(
                                    archive ->
                                            archive.getIngestionDate() != null
                                                    && archive.getIngestionDate()
                                                            .toLocalDate()
                                                            .equals(today))
                            .map(FileArchive::getFileName)
                            .collect(Collectors.toList());

            // Fetch all records from the database
            List<RuleCounterRecord> records = repository.findAll();

            // Filter by the requested flow type AND files ingested today
            int totalRejected =
                    records.stream()
                            .filter(
                                    r ->
                                            r.getFileName() != null
                                                    && filesToday.contains(r.getFileName()))
                            .filter(
                                    r -> {
                                        if (request.flowType() == null
                                                || request.flowType().trim().isEmpty()
                                                || request.flowType().equalsIgnoreCase("ALL")) {
                                            return true; // Don't filter by flow type if ALL is
                                            // requested
                                        }
                                        return r.getTypeFlux() != null
                                                && r.getTypeFlux()
                                                        .trim()
                                                        .equalsIgnoreCase(
                                                                request.flowType().trim());
                                    })
                            .mapToInt(
                                    r -> {
                                        try {
                                            // The database stores these as strings, so we parse
                                            // them safely
                                            return Integer.parseInt(r.getCreRejetes().trim());
                                        } catch (Exception e) {
                                            return 0;
                                        }
                                    })
                            .sum();

            log.info(
                    "Database calculated {} rejections for {} today.",
                    totalRejected,
                    request.flowType());
            return new MetricsResponse(totalRejected);
        };
    }

    @Cacheable(value = "metrics", key = "#targetDate != null ? #targetDate.toString() : 'today'")
    public String getMetricsSummaryForDate(LocalDate targetDate) {
        if (targetDate == null) {
            targetDate = LocalDate.now();
        }

        final LocalDate finalTargetDate = targetDate;

        List<String> filesOnDate =
                archiveRepository.findAll().stream()
                        .filter(
                                archive ->
                                        archive.getIngestionDate() != null
                                                && archive.getIngestionDate()
                                                        .toLocalDate()
                                                        .equals(finalTargetDate))
                        .map(FileArchive::getFileName)
                        .collect(Collectors.toList());

        List<RuleCounterRecord> records =
                repository.findAll().stream()
                        .filter(
                                r ->
                                        r.getFileName() != null
                                                && filesOnDate.contains(r.getFileName()))
                        .collect(Collectors.toList());

        long creReceived = sumField(records, "CRE", "recus");
        long ecReceived = sumField(records, "EC", "recus");

        long creRejected = sumField(records, "CRE", "rejetes");
        long ecRejected = sumField(records, "EC", "rejetes");

        long creTreated = sumField(records, "CRE", "traites");
        long ecTreated = sumField(records, "EC", "traites");

        return String.format(
                "Metrics Data for %s. "
                        + "Received: %d : CRE: %d, EC: %d. "
                        + "Rejected: %d : CRE: %d, EC: %d. "
                        + "Treated Correctly: %d : CRE: %d, EC: %d.",
                finalTargetDate.toString(),
                (creReceived + ecReceived),
                creReceived,
                ecReceived,
                (creRejected + ecRejected),
                creRejected,
                ecRejected,
                (creTreated + ecTreated),
                creTreated,
                ecTreated);
    }

    @Cacheable(value = "metrics", key = "'all-time'")
    public String getAllTimeMetricsSummary() {
        List<RuleCounterRecord> records = repository.findAll();

        long creReceived = sumField(records, "CRE", "recus");
        long ecReceived = sumField(records, "EC", "recus");

        long creRejected = sumField(records, "CRE", "rejetes");
        long ecRejected = sumField(records, "EC", "rejetes");

        long creTreated = sumField(records, "CRE", "traites");
        long ecTreated = sumField(records, "EC", "traites");

        return String.format(
                "Metrics Data for ALL TIME. "
                        + "Received: %d : CRE: %d, EC: %d. "
                        + "Rejected: %d : CRE: %d, EC: %d. "
                        + "Treated Correctly: %d : CRE: %d, EC: %d.",
                (creReceived + ecReceived),
                creReceived,
                ecReceived,
                (creRejected + ecRejected),
                creRejected,
                ecRejected,
                (creTreated + ecTreated),
                creTreated,
                ecTreated);
    }

    private long sumField(List<RuleCounterRecord> records, String recordType, String fieldName) {
        return records.stream()
                .filter(
                        r ->
                                r.getRecordType() != null
                                        && r.getRecordType().trim().equalsIgnoreCase(recordType))
                .mapToLong(
                        r -> {
                            String val = "";
                            if ("recus".equals(fieldName)) val = r.getCreRecus();
                            else if ("rejetes".equals(fieldName)) val = r.getCreRejetes();
                            else if ("traites".equals(fieldName)) val = r.getCreTraites();

                            try {
                                return val != null && !val.trim().isEmpty()
                                        ? Long.parseLong(val.trim())
                                        : 0L;
                            } catch (Exception e) {
                                return 0L;
                            }
                        })
                .sum();
    }
}

package com.xrdj.iris.controller;

import com.xrdj.iris.model.AccountingTreatment;
import com.xrdj.iris.model.AnomalyRecord;
import com.xrdj.iris.repository.AnomalyRecordRepository;
import com.xrdj.iris.service.TreatmentService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TreatmentService treatmentService;
    private final AnomalyRecordRepository anomalyRecordRepository;

    public DashboardController(
            TreatmentService treatmentService, AnomalyRecordRepository anomalyRecordRepository) {
        this.treatmentService = treatmentService;
        this.anomalyRecordRepository = anomalyRecordRepository;
    }

    @GetMapping("/treatments")
    public List<AccountingTreatment> getTreatments() {
        return treatmentService.getAllTreatments();
    }

    @GetMapping("/rejections-summary")
    public Map<String, Long> getRejectionsSummary() {
        return treatmentService.getAllTreatments().stream()
                .filter(t -> t.getNbCreRejetes() > 0)
                .collect(
                        Collectors.groupingBy(
                                AccountingTreatment::getNomApplication,
                                Collectors.summingLong(AccountingTreatment::getNbCreRejetes)));
    }

    @GetMapping("/anomalies")
    public List<AnomalyRecord> getAnomalies() {
        return anomalyRecordRepository.findAll();
    }
}

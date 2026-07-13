package com.xrdj.iris.controller;

import com.xrdj.iris.model.AccountingTreatment;
import com.xrdj.iris.service.TreatmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TreatmentService treatmentService;

    public DashboardController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping("/treatments")
    public List<AccountingTreatment> getTreatments() {
        return treatmentService.getAllTreatments();
    }

    @GetMapping("/rejections-summary")
    public Map<String, Long> getRejectionsSummary() {
        return treatmentService.getAllTreatments().stream()
                .filter(t -> t.getNbCreRejetes() > 0)
                .collect(Collectors.groupingBy(
                        AccountingTreatment::getTypeFlux,
                        Collectors.summingLong(AccountingTreatment::getNbCreRejetes)
                ));
    }
}

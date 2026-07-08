package com.xrdj.iris.controller;

import com.xrdj.iris.dto.SystemHealthDto;
import com.xrdj.iris.service.TreatmentMockService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TreatmentMockService treatmentService;

    public AdminController(TreatmentMockService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping("/health")
    public SystemHealthDto getHealth() {
        // Mock system health
        int errorCount = (int) treatmentService.getAllTreatments().stream()
                .mapToLong(t -> t.getNbCreRejetes())
                .sum();
        return new SystemHealthDto(5, 12, errorCount);
    }

    @PostMapping("/trigger-ingestion")
    public void triggerIngestion() {
        treatmentService.triggerManualIngestion();
    }

    @PostMapping("/purge-cache")
    public void purgeCache() {
        treatmentService.purgeRejectedCache();
    }

    @PostMapping("/reprocess/{id}")
    public void reprocessTreatment(@PathVariable String id) {
        treatmentService.reprocessTreatment(id);
    }
}

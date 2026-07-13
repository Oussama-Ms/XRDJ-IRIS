package com.xrdj.iris.controller;

import com.xrdj.iris.dto.SystemHealthDto;
import com.xrdj.iris.service.TreatmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TreatmentService treatmentService;

    public AdminController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @GetMapping("/health")
    public SystemHealthDto getHealth() {
        return treatmentService.getSystemHealth();
    }

    @PostMapping("/trigger-ingestion")
    public void triggerIngestion() {
        treatmentService.triggerManualIngestion();
    }

    @DeleteMapping("/clear-data")
    public void clearData() {
        treatmentService.clearAllData();
    }

    @PostMapping("/reprocess/{id}")
    public void reprocessTreatment(@PathVariable String id) {
        treatmentService.reprocessTreatment(id);
    }
}

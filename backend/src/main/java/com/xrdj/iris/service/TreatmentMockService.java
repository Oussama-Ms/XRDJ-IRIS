package com.xrdj.iris.service;

import com.xrdj.iris.model.AccountingTreatment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TreatmentMockService {

    private final List<AccountingTreatment> treatments = new ArrayList<>();

    public TreatmentMockService() {
        // Initialize with some mock data
        treatments.add(new AccountingTreatment(UUID.randomUUID().toString(), LocalDateTime.now().minusHours(2), "APP_CORE", "Dotation", 1000L, 950L, 50L, 2000L, "Rejeté partiellement"));
        treatments.add(new AccountingTreatment(UUID.randomUUID().toString(), LocalDateTime.now().minusHours(1), "APP_RETAIL", "Engagement", 500L, 500L, 0L, 1000L, "Traité complètement"));
        treatments.add(new AccountingTreatment(UUID.randomUUID().toString(), LocalDateTime.now().minusMinutes(30), "APP_CORP", "Remboursement", 200L, 0L, 200L, 0L, "Rejeté complètement"));
    }

    public List<AccountingTreatment> getAllTreatments() {
        return new ArrayList<>(treatments);
    }

    public void triggerManualIngestion() {
        // Mocking a .seq file read
        treatments.add(new AccountingTreatment(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "APP_SEQ_MOCK",
                "Frais",
                150L,
                150L,
                0L,
                300L,
                "Traité complètement"
        ));
    }

    public void purgeRejectedCache() {
        treatments.removeIf(t -> "Rejeté complètement".equals(t.getStatut()) || "Rejeté partiellement".equals(t.getStatut()));
    }

    public void reprocessTreatment(String id) {
        for (AccountingTreatment t : treatments) {
            if (t.getId().equals(id)) {
                t.setNbCreTraites(t.getNbCreRecus());
                t.setNbCreRejetes(0L);
                t.setStatut("Traité complètement");
                break;
            }
        }
    }
}

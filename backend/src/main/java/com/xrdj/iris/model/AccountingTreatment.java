package com.xrdj.iris.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingTreatment {
    private String id;
    private LocalDateTime dateTraitement;
    private String nomApplication;
    private String typeFlux;
    private Long nbCreRecus;
    private Long nbCreTraites;
    private Long nbCreRejetes;
    private Long nbMeGeneres;
    private String statut;
}

package com.xrdj.iris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "anomaly_records")
public class AnomalyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String creType;
    private String codApp;
    private String batchid;
    private String idCre;
    private String codeErreur;
    @Column(length = 1000)
    private String texteErreur;
    private String nbrRecyclage;
    private String darDate;
    private String darTime;
    private String codLot;
    private String codAgence;
    private String datOperat;
    private String deviseOp;
    private String mntOperat;
    @Column(length = 1000)
    private String codeRecyclage;
    private String codePhase;
    private String codeDomaine;
    @Column(length = 1000)
    private String texteCompErreur;
    private String emetteur;
    private String numCreErr;
    private String codeCre;
    private String versionCre;
    private String codeInstance;
    private String numAno;
    private String typAno;
    private String nivGene;
    private String origineAno;
    private String indEnregCre;
    private String codeEnreg;
    private String typeRegle;
    private String codeRegleEnreg;
    private String debVersionRegleEnreg;
    private String finVersionRegleEnreg;
    private String codeRegleMe;
    private String debVersionRegleMe;
    private String finVersionRegleMe;
    private String code;
    private String idfVacation;
    private String idfEtape;
    private String dateVacation;
    private String heureVacation;
    private String lot;
    private String nivDetect;
    private String codePrioSchema;
    private String codeSchema;
    private String numSeqGarn;
    private String adrGarn;
    private String codeFormatMe;
    private String mnemoModule;
    private String codeEtatAutom;
    private String lgEnreg;
    @Column(length = 2000)
    private String enreg;

    private String fileName;

}

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

    private String recordType;
    private String idEbs;
    private String errorCode;
    
    @Column(length = 1000)
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String remainingData; // For whatever is left that you haven't mapped yet

}

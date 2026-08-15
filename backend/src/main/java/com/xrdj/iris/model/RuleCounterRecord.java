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
@Table(name = "rule_counter_records")
public class RuleCounterRecord implements org.springframework.batch.item.ResourceAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Override
    public void setResource(org.springframework.core.io.Resource resource) {
        this.fileName = resource.getFilename();
    }

    private String recordType;
    private String appCode1;
    private String appCode2;
    private String typeFlux;
    private String dateStart;
    private String dateEnd;
    private String creRecus;
    private String creTraites;
    private String creRejetes;
    private String meGeneres;
}

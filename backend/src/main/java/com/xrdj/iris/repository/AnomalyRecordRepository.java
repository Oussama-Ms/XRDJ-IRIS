package com.xrdj.iris.repository;

import com.xrdj.iris.model.AnomalyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyRecordRepository extends JpaRepository<AnomalyRecord, Long> {
}

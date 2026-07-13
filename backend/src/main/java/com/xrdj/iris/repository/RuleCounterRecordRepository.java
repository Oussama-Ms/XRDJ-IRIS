package com.xrdj.iris.repository;

import com.xrdj.iris.model.RuleCounterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleCounterRecordRepository extends JpaRepository<RuleCounterRecord, Long> {
    boolean existsByFileName(String fileName);
}

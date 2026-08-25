package com.emcs.repository;

import com.emcs.entity.ControlRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ControlRecordRepository extends JpaRepository<ControlRecord, Long>, JpaSpecificationExecutor<ControlRecord> {
}

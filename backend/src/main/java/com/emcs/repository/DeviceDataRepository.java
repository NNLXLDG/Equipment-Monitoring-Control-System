package com.emcs.repository;

import com.emcs.entity.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DeviceDataRepository extends JpaRepository<DeviceData, Long>, JpaSpecificationExecutor<DeviceData> {

    List<DeviceData> findByDevIdAndPointCodeAndCollectTimeBetweenOrderByCollectTimeAsc(
            String devId, String pointCode, LocalDateTime start, LocalDateTime end);

    DeviceData findTopByDevIdAndPointCodeOrderByCollectTimeDesc(String devId, String pointCode);

    long countByCollectTimeAfter(LocalDateTime time);

    @Query("select max(d.numValue), min(d.numValue), avg(d.numValue), count(d) " +
            "from DeviceData d where d.devId = ?1 and d.pointCode = ?2 " +
            "and d.collectTime between ?3 and ?4")
    List<Object[]> stats(String devId, String pointCode, LocalDateTime start, LocalDateTime end);
}

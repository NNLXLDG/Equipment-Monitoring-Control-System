package com.emcs.repository;

import com.emcs.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    Optional<Device> findByDevId(String devId);
    boolean existsByDevId(String devId);
    long countByStatus(Integer status);
}

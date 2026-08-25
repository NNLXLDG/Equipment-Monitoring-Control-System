package com.emcs.repository;

import com.emcs.entity.DevicePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DevicePointRepository extends JpaRepository<DevicePoint, Long>, JpaSpecificationExecutor<DevicePoint> {
    List<DevicePoint> findByDeviceIdOrderByPointCode(Long deviceId);
    boolean existsByDeviceIdAndPointCode(Long deviceId, String pointCode);
    Optional<DevicePoint> findByDeviceIdAndPointCode(Long deviceId, String pointCode);
}

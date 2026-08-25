package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.common.PageResult;
import com.emcs.entity.Device;
import com.emcs.repository.DevicePointRepository;
import com.emcs.repository.DeviceRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DevicePointRepository pointRepository;

    public PageResult<Device> page(String keyword, Integer status, String deviceType, int page, int size) {
        Specification<Device> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("devId"), like),
                        cb.like(root.get("deviceName"), like),
                        cb.like(root.get("location"), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(deviceType)) {
                predicates.add(cb.equal(root.get("deviceType"), deviceType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Device> result = deviceRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id")));
        return PageResult.of(result);
    }

    public List<Device> listAll() {
        return deviceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Device get(Long id) {
        return deviceRepository.findById(id).orElseThrow(() -> new BusinessException("设备不存在"));
    }

    @Transactional
    public void create(Device device) {
        if (!StringUtils.hasText(device.getDevId()) || !StringUtils.hasText(device.getDeviceName())) {
            throw new BusinessException("设备编号和设备名称不能为空");
        }
        if (deviceRepository.existsByDevId(device.getDevId())) {
            throw new BusinessException("设备编号已存在");
        }
        device.setId(null);
        if (device.getStatus() == null) device.setStatus(0);
        deviceRepository.save(device);
    }

    @Transactional
    public void update(Long id, Device device) {
        Device d = deviceRepository.findById(id).orElseThrow(() -> new BusinessException("设备不存在"));
        d.setDeviceName(device.getDeviceName());
        d.setDeviceType(device.getDeviceType());
        d.setModel(device.getModel());
        d.setManufacturer(device.getManufacturer());
        d.setLocation(device.getLocation());
        d.setStatus(device.getStatus() == null ? 0 : device.getStatus());
        d.setInstallDate(device.getInstallDate());
        d.setDescription(device.getDescription());
        deviceRepository.save(d);
    }

    @Transactional
    public void delete(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new BusinessException("设备不存在"));
        // 删除关联点位
        pointRepository.findByDeviceIdOrderByPointCode(id).forEach(p -> pointRepository.delete(p));
        deviceRepository.delete(device);
    }
}

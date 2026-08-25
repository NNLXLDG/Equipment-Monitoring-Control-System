package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.common.PageResult;
import com.emcs.entity.DevicePoint;
import com.emcs.repository.DevicePointRepository;
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
public class DevicePointService {

    private final DevicePointRepository pointRepository;

    public PageResult<DevicePoint> page(Long deviceId, Integer pointType, int page, int size) {
        Specification<DevicePoint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (deviceId != null) {
                predicates.add(cb.equal(root.get("deviceId"), deviceId));
            }
            if (pointType != null) {
                predicates.add(cb.equal(root.get("pointType"), pointType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<DevicePoint> result = pointRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "pointCode")));
        return PageResult.of(result);
    }

    public DevicePoint get(Long id) {
        return pointRepository.findById(id).orElseThrow(() -> new BusinessException("点位不存在"));
    }

    public List<DevicePoint> listByDevice(Long deviceId) {
        return pointRepository.findByDeviceIdOrderByPointCode(deviceId);
    }

    @Transactional
    public void create(DevicePoint point) {
        if (point.getDeviceId() == null || !StringUtils.hasText(point.getPointCode())) {
            throw new BusinessException("设备和点位编码不能为空");
        }
        if (pointRepository.existsByDeviceIdAndPointCode(point.getDeviceId(), point.getPointCode())) {
            throw new BusinessException("该设备的点位编码已存在");
        }
        point.setId(null);
        if (point.getPointType() == null) point.setPointType(1);
        if (point.getDataType() == null) point.setDataType(1);
        pointRepository.save(point);
    }

    @Transactional
    public void update(Long id, DevicePoint point) {
        DevicePoint p = pointRepository.findById(id).orElseThrow(() -> new BusinessException("点位不存在"));
        p.setPointName(point.getPointName());
        p.setPointType(point.getPointType());
        p.setDataType(point.getDataType());
        p.setUnit(point.getUnit());
        p.setMinValue(point.getMinValue());
        p.setMaxValue(point.getMaxValue());
        p.setDefaultValue(point.getDefaultValue());
        p.setDescription(point.getDescription());
        pointRepository.save(p);
    }

    @Transactional
    public void delete(Long id) {
        pointRepository.deleteById(id);
    }
}

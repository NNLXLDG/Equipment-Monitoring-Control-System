package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.common.PageResult;
import com.emcs.entity.Device;
import com.emcs.entity.DeviceData;
import com.emcs.entity.DevicePoint;
import com.emcs.repository.DeviceDataRepository;
import com.emcs.repository.DevicePointRepository;
import com.emcs.repository.DeviceRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DeviceDataRepository dataRepository;
    private final DeviceRepository deviceRepository;
    private final DevicePointRepository pointRepository;

    public PageResult<DeviceData> page(String devId, String pointCode, LocalDateTime start, LocalDateTime end,
                                       int page, int size) {
        Specification<DeviceData> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (devId != null && !devId.isBlank()) {
                predicates.add(cb.equal(root.get("devId"), devId));
            }
            if (pointCode != null && !pointCode.isBlank()) {
                predicates.add(cb.equal(root.get("pointCode"), pointCode));
            }
            if (start != null) predicates.add(cb.greaterThanOrEqualTo(root.get("collectTime"), start));
            if (end != null) predicates.add(cb.lessThanOrEqualTo(root.get("collectTime"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<DeviceData> result = dataRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "collectTime")));
        return PageResult.of(result);
    }

    /**
     * 设备各采集点最新值
     */
    public List<Map<String, Object>> latest(String devId) {
        Device device = deviceRepository.findByDevId(devId)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        List<DevicePoint> points = pointRepository.findByDeviceIdOrderByPointCode(device.getId())
                .stream().filter(p -> p.getPointType() != null && p.getPointType() == 1).toList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (DevicePoint p : points) {
            DeviceData d = dataRepository.findTopByDevIdAndPointCodeOrderByCollectTimeDesc(devId, p.getPointCode());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("pointCode", p.getPointCode());
            m.put("pointName", p.getPointName());
            m.put("unit", p.getUnit());
            m.put("value", d == null ? null : d.getValue());
            m.put("collectTime", d == null ? null : d.getCollectTime().format(FMT));
            result.add(m);
        }
        return result;
    }

    /**
     * 折线图数据
     */
    public Map<String, Object> chart(String devId, String pointCode, LocalDateTime start, LocalDateTime end) {
        List<DeviceData> list = dataRepository
                .findByDevIdAndPointCodeAndCollectTimeBetweenOrderByCollectTimeAsc(devId, pointCode, start, end);
        List<String> times = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (DeviceData d : list) {
            times.add(d.getCollectTime().format(FMT));
            values.add(d.getNumValue() != null ? d.getNumValue() : d.getValue());
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("times", times);
        m.put("values", values);
        return m;
    }

    /**
     * 点位统计（max/min/avg/count）
     */
    public Map<String, Object> pointStats(String devId, String pointCode, LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = dataRepository.stats(devId, pointCode, start, end);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("devId", devId);
        m.put("pointCode", pointCode);
        Device device = deviceRepository.findByDevId(devId).orElse(null);
        if (device != null) {
            pointRepository.findByDeviceIdAndPointCode(device.getId(), pointCode).ifPresent(p -> {
                m.put("pointName", p.getPointName());
                m.put("unit", p.getUnit());
            });
        }
        if (rows != null && !rows.isEmpty() && rows.get(0) != null) {
            Object[] r = rows.get(0);
            m.put("max", r[0]);
            m.put("min", r[1]);
            m.put("avg", r[2]);
            m.put("count", r[3]);
        } else {
            m.put("max", null);
            m.put("min", null);
            m.put("avg", null);
            m.put("count", 0);
        }
        return m;
    }
}

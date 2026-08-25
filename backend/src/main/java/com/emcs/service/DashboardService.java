package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.entity.Device;
import com.emcs.entity.DevicePoint;
import com.emcs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final DevicePointRepository pointRepository;
    private final DeviceDataRepository dataRepository;
    private final ControlRecordRepository controlRecordRepository;
    private final DataService dataService;

    public Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("deviceTotal", deviceRepository.count());
        m.put("deviceOnline", deviceRepository.countByStatus(1));
        m.put("pointTotal", pointRepository.count());
        m.put("dataToday", dataRepository.countByCollectTimeAfter(LocalDate.now().atStartOfDay()));
        m.put("controlTotal", controlRecordRepository.count());
        return m;
    }

    public List<Map<String, Object>> deviceTypeDist() {
        Map<String, Long> group = new LinkedHashMap<>();
        for (Device d : deviceRepository.findAll()) {
            String type = d.getDeviceType() == null || d.getDeviceType().isBlank() ? "未分类" : d.getDeviceType();
            group.merge(type, 1L, Long::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        group.forEach((k, v) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", k);
            item.put("value", v);
            result.add(item);
        });
        return result;
    }

    public Map<String, Object> dataTrend(String devId, String pointCode, int hours) {
        if (devId == null || devId.isBlank()) {
            throw new BusinessException("请选择设备");
        }
        if (pointCode == null || pointCode.isBlank()) {
            Device device = deviceRepository.findByDevId(devId)
                    .orElseThrow(() -> new BusinessException("设备不存在"));
            pointCode = pointRepository.findByDeviceIdOrderByPointCode(device.getId()).stream()
                    .filter(p -> p.getPointType() != null && p.getPointType() == 1)
                    .map(DevicePoint::getPointCode)
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("设备无采集点"));
        }
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hours);
        return dataService.chart(devId, pointCode, start, end);
    }
}

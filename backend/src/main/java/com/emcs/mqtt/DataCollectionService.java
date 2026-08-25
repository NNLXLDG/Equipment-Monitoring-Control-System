package com.emcs.mqtt;

import com.emcs.entity.Device;
import com.emcs.entity.DeviceData;
import com.emcs.entity.DevicePoint;
import com.emcs.repository.DeviceDataRepository;
import com.emcs.repository.DevicePointRepository;
import com.emcs.repository.DeviceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * MQTT 采集数据处理：解析 JSON 数据帧并写入数据库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectionService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final ObjectMapper objectMapper;
    private final DeviceRepository deviceRepository;
    private final DevicePointRepository pointRepository;
    private final DeviceDataRepository dataRepository;

    @Transactional
    public void handle(String topic, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String devId = root.path("devId").asText();
            String msgType = root.path("msgType").asText();

            if (devId.isBlank()) {
                log.warn("消息缺少 devId, topic={}", topic);
                return;
            }

            if ("aiValueRpt".equals(msgType)) {
                handleAiReport(devId, root);
            } else if ("doValueRpt".equals(msgType)) {
                handleDoReport(devId, root);
            } else {
                log.debug("忽略未知 msgType={}", msgType);
            }
        } catch (Exception e) {
            log.error("解析 MQTT 数据帧失败: {}", payload, e);
        }
    }

    private void handleAiReport(String devId, JsonNode root) {
        Device device = deviceRepository.findByDevId(devId).orElse(null);
        if (device == null) {
            log.warn("收到未知设备的上报数据 devId={}", devId);
            return;
        }
        // 更新设备在线状态
        if (device.getStatus() == null || device.getStatus() != 1) {
            device.setStatus(1);
            deviceRepository.save(device);
        }

        JsonNode dataNode = root.path("data");
        LocalDateTime collectTime = parseTimestamp(root.path("timestamp").asText());

        List<DeviceData> list = new ArrayList<>();
        dataNode.fields().forEachRemaining(entry -> {
            String pointCode = entry.getKey();
            String value = entry.getValue().asText();
            DevicePoint point = pointRepository.findByDeviceIdAndPointCode(device.getId(), pointCode).orElse(null);

            DeviceData d = new DeviceData();
            d.setDeviceId(device.getId());
            d.setDevId(devId);
            d.setPointCode(pointCode);
            d.setPointName(point != null ? point.getPointName() : pointCode);
            d.setUnit(point != null ? point.getUnit() : null);
            d.setValue(value);
            d.setNumValue(parseNumber(value));
            d.setCollectTime(collectTime);
            list.add(d);
        });

        dataRepository.saveAll(list);
        log.info("已入库设备 {} 采集数据 {} 条", devId, list.size());
    }

    private void handleDoReport(String devId, JsonNode root) {
        // 设备回传的控制状态（可扩展：更新控制记录为执行成功）
        log.info("收到设备 {} 控制状态回传: {}", devId, root.toString());
    }

    private LocalDateTime parseTimestamp(String ts) {
        try {
            long epoch = Long.parseLong(ts);
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZONE);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private Double parseNumber(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
}

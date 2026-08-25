package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.common.PageResult;
import com.emcs.entity.ControlRecord;
import com.emcs.entity.Device;
import com.emcs.entity.DevicePoint;
import com.emcs.entity.User;
import com.emcs.mqtt.MqttService;
import com.emcs.repository.ControlRecordRepository;
import com.emcs.repository.DevicePointRepository;
import com.emcs.repository.DeviceRepository;
import com.emcs.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ControlService {

    private final DeviceRepository deviceRepository;
    private final DevicePointRepository pointRepository;
    private final ControlRecordRepository recordRepository;
    private final MqttService mqttService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> send(String devId, Map<String, String> data) {
        if (devId == null || devId.isBlank()) {
            throw new BusinessException("设备编号不能为空");
        }
        if (data == null || data.isEmpty()) {
            throw new BusinessException("控制参数不能为空");
        }
        Device device = deviceRepository.findByDevId(devId)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        User operator = SecurityUtils.getCurrentUser();

        // 封装 doValueRpt 控制帧
        Map<String, Object> frame = new LinkedHashMap<>();
        frame.put("devId", devId);
        frame.put("msgType", "doValueRpt");
        frame.put("data", data);
        frame.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        String payload;
        try {
            payload = objectMapper.writeValueAsString(frame);
        } catch (Exception e) {
            throw new BusinessException("控制帧封装失败");
        }

        String topic = "/" + devId + "/sub";
        boolean ok = mqttService.publish(topic, payload);

        // 记录控制日志（每个控制点一条）
        List<Map<String, Object>> records = new ArrayList<>();
        data.forEach((code, value) -> {
            DevicePoint point = pointRepository.findByDeviceIdAndPointCode(device.getId(), code).orElse(null);
            ControlRecord record = new ControlRecord();
            record.setDeviceId(device.getId());
            record.setDevId(devId);
            record.setPointCode(code);
            record.setPointName(point != null ? point.getPointName() : code);
            record.setValue(value);
            record.setMsgType("doValueRpt");
            record.setOperatorId(operator.getId());
            record.setOperatorName(operator.getRealName() != null ? operator.getRealName() : operator.getUsername());
            record.setStatus(ok ? 0 : 2);
            record.setResponse(ok ? "已发送" : "发送失败：MQTT 未连接");
            recordRepository.save(record);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("pointCode", code);
            m.put("value", value);
            m.put("status", ok);
            records.add(m);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topic", topic);
        result.put("payload", payload);
        result.put("sent", ok);
        result.put("records", records);
        return result;
    }

    public PageResult<ControlRecord> records(String devId, int page, int size) {
        Specification<ControlRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (devId != null && !devId.isBlank()) {
                predicates.add(cb.equal(root.get("devId"), devId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ControlRecord> result = recordRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        return PageResult.of(result);
    }
}

package com.emcs.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 设备数据模拟器：模拟采集器周期性向 /kettle/pub 上报运行数据，
 * 并订阅 /{devId}/sub 控制主题，用于开发/演示。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "simulator.enabled", havingValue = "true")
public class DeviceSimulator {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${simulator.interval:5}")
    private int intervalSeconds;

    @Value("${simulator.device-ids:}")
    private String deviceIds;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MqttClient client;
    private ScheduledExecutorService scheduler;
    private final List<String> devices = new ArrayList<>();

    @PostConstruct
    public void start() {
        if (deviceIds != null && !deviceIds.isBlank()) {
            devices.addAll(Arrays.stream(deviceIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        }
        if (devices.isEmpty()) {
            log.warn("模拟器未配置设备，跳过启动");
            return;
        }
        try {
            client = new MqttClient(broker, "emcs-simulator-" + System.currentTimeMillis(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            // 订阅每台设备的控制主题
            for (String devId : devices) {
                client.subscribe("/" + devId + "/sub", 1);
            }
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("模拟器 MQTT 断开: {}", cause == null ? "" : cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    log.info("设备收到控制指令 topic={} payload={}", topic,
                            new String(message.getPayload(), StandardCharsets.UTF_8));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleWithFixedDelay(this::publishData, 2, intervalSeconds, TimeUnit.SECONDS);
            log.info("设备数据模拟器已启动，间隔 {} 秒，设备数 {}", intervalSeconds, devices.size());
        } catch (MqttException e) {
            log.error("设备模拟器启动失败: {}", e.getMessage());
        }
    }

    private void publishData() {
        if (client == null || !client.isConnected()) {
            try {
                if (client != null && !client.isConnected()) client.connect();
            } catch (MqttException e) {
                log.warn("模拟器重连失败: {}", e.getMessage());
                return;
            }
        }
        for (String devId : devices) {
            try {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("AI1", String.valueOf(ThreadLocalRandom.current().nextInt(0, 101)));       // 温度
                data.put("AI2", String.valueOf(ThreadLocalRandom.current().nextInt(4000, 5001)));  // 水位
                data.put("AI3", String.valueOf(ThreadLocalRandom.current().nextInt(0, 2)));        // 加热状态
                data.put("AI4", String.valueOf(ThreadLocalRandom.current().nextInt(0, 2001)));     // 功率

                Map<String, Object> frame = new LinkedHashMap<>();
                frame.put("devId", devId);
                frame.put("msgType", "aiValueRpt");
                frame.put("data", data);
                frame.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

                String payload = objectMapper.writeValueAsString(frame);
                client.publish("/kettle/pub", new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                log.error("模拟器上报数据失败 devId={}: {}", devId, e.getMessage());
            }
        }
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
        try {
            if (client != null && client.isConnected()) client.disconnect();
        } catch (MqttException ignored) {
        }
    }
}

package com.emcs.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MQTT 客户端服务：订阅 /kettle/pub 采集主题，支持断线重连与消息发布。
 */
@Slf4j
@Component
public class MqttService implements MqttCallbackExtended {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.subscribe-topic}")
    private String subscribeTopic;

    @Value("${mqtt.qos:1}")
    private int qos;

    private MqttClient client;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final DataCollectionService dataCollectionService;

    public MqttService(DataCollectionService dataCollectionService) {
        this.dataCollectionService = dataCollectionService;
    }

    @PostConstruct
    public void start() {
        ensureClient();
        connect();
    }

    private void ensureClient() {
        if (client == null) {
            try {
                client = new MqttClient(broker, clientId, new MemoryPersistence());
                client.setCallback(this);
            } catch (MqttException e) {
                log.error("MQTT 客户端初始化失败: {}", e.getMessage());
                client = null;
            }
        }
    }

    /**
     * 定时检测并重连（首次连接失败或 Broker 重启后自动恢复）
     */
    @Scheduled(fixedDelay = 10000)
    public void reconnectIfNeeded() {
        if (!isConnected()) {
            ensureClient();
            connect();
        }
    }

    private void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            if (username != null && !username.isBlank()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }
            log.info("MQTT 连接中: {}", broker);
            client.connect(options);
        } catch (MqttException e) {
            log.error("MQTT 连接失败: {} ({}), 稍后重试", broker, e.getMessage());
            connected.set(false);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        connected.set(true);
        log.info("MQTT 已连接: {} (reconnect={})", serverURI, reconnect);
        try {
            client.subscribe(subscribeTopic, qos);
            log.info("已订阅主题: {}", subscribeTopic);
        } catch (MqttException e) {
            log.error("订阅主题失败: {}", e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        connected.set(false);
        log.warn("MQTT 连接断开: {}", cause == null ? "unknown" : cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        log.debug("收到消息 topic={} payload={}", topic, payload);
        try {
            dataCollectionService.handle(topic, payload);
        } catch (Exception e) {
            log.error("处理 MQTT 消息异常", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 无需处理
    }

    /**
     * 发布消息
     */
    public boolean publish(String topic, String payload) {
        if (!isConnected()) {
            log.warn("MQTT 未连接，无法发布到 {}", topic);
            return false;
        }
        try {
            MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(qos);
            msg.setRetained(false);
            client.publish(topic, msg);
            log.info("发布消息 topic={} payload={}", topic, payload);
            return true;
        } catch (MqttException e) {
            log.error("发布消息失败 topic={}: {}", topic, e.getMessage());
            return false;
        }
    }

    public boolean isConnected() {
        return connected.get() && client != null && client.isConnected();
    }

    @PreDestroy
    public void stop() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
            if (client != null) {
                client.close();
            }
        } catch (MqttException e) {
            log.warn("MQTT 关闭异常: {}", e.getMessage());
        }
    }
}

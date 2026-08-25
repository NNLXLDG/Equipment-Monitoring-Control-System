package com.emcs.mqtt;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * 内嵌 MQTT Broker（Moquette），用于开发/演示，可关闭后连接外部 Broker。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mqtt.embedded.enabled", havingValue = "true")
public class EmbeddedBroker {

    @Value("${mqtt.embedded.host:0.0.0.0}")
    private String host;

    @Value("${mqtt.embedded.port:1883}")
    private int port;

    private Server server;

    @PostConstruct
    public void start() {
        try {
            Properties props = new Properties();
            props.setProperty(BrokerConstants.HOST_PROPERTY_NAME, host);
            props.setProperty(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(port));
            props.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, "true");

            server = new Server();
            server.startServer(new MemoryConfig(props));
            log.info("内嵌 MQTT Broker 已启动: {}:{}", host, port);
        } catch (Exception e) {
            log.error("内嵌 MQTT Broker 启动失败: {} (请检查端口占用或关闭内嵌 Broker)", e.getMessage());
        }
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stopServer();
            log.info("内嵌 MQTT Broker 已停止");
        }
    }
}

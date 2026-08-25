package com.emcs.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 设备唯一标识（MQTT 主题用） */
    @Column(name = "dev_id", nullable = false, unique = true, length = 32)
    private String devId;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(length = 50)
    private String model;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String location;

    /** 1 在线 0 离线 */
    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Column(length = 255)
    private String description;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}

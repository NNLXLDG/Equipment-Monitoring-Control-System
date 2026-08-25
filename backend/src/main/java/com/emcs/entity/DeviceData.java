package com.emcs.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_data", indexes = {
        @Index(name = "idx_data_dev_time", columnList = "dev_id,point_code,collect_time")
})
public class DeviceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "dev_id", nullable = false, length = 32)
    private String devId;

    @Column(name = "point_code", nullable = false, length = 16)
    private String pointCode;

    @Column(name = "point_name", length = 50)
    private String pointName;

    @Column(length = 64)
    private String value;

    @Column(name = "num_value")
    private Double numValue;

    @Column(length = 20)
    private String unit;

    @Column(name = "collect_time", nullable = false)
    private LocalDateTime collectTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) createTime = LocalDateTime.now();
    }
}

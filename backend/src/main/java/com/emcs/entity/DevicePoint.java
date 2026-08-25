package com.emcs.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_point", uniqueConstraints = {
        @UniqueConstraint(name = "uk_point_dev_code", columnNames = {"device_id", "point_code"})
})
public class DevicePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "point_code", nullable = false, length = 16)
    private String pointCode;

    @Column(name = "point_name", nullable = false, length = 50)
    private String pointName;

    /** 1 采集(AI) 2 控制(DO) */
    @Column(name = "point_type", nullable = false)
    private Integer pointType = 1;

    /** 1 数值 2 布尔 */
    @Column(name = "data_type", nullable = false)
    private Integer dataType = 1;

    @Column(length = 20)
    private String unit;

    @Column(name = "min_value")
    private BigDecimal minValue;

    @Column(name = "max_value")
    private BigDecimal maxValue;

    @Column(name = "default_value", length = 32)
    private String defaultValue;

    @Column(length = 255)
    private String description;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) createTime = LocalDateTime.now();
    }
}

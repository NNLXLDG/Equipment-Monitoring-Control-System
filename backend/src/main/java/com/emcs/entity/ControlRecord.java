package com.emcs.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "control_record", indexes = {
        @Index(name = "idx_control_dev", columnList = "dev_id")
})
public class ControlRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "dev_id", nullable = false, length = 32)
    private String devId;

    @Column(name = "point_code", length = 16)
    private String pointCode;

    @Column(name = "point_name", length = 50)
    private String pointName;

    @Column(length = 32)
    private String value;

    @Column(name = "msg_type", length = 32)
    private String msgType;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    /** 0 已发送 1 执行成功 2 失败 */
    @Column(nullable = false)
    private Integer status = 0;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) createTime = LocalDateTime.now();
    }
}

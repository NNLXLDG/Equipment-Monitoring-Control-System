package com.emcs.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ControlSendRequest {
    private String devId;
    private Map<String, String> data;
}

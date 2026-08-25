package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.common.PageResult;
import com.emcs.entity.DeviceData;
import com.emcs.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;

    @GetMapping
    public ApiResult<PageResult<DeviceData>> page(
            @RequestParam(required = false) String devId,
            @RequestParam(required = false) String pointCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(dataService.page(devId, pointCode, start, end, page, size));
    }

    @GetMapping("/latest")
    public ApiResult<List<Map<String, Object>>> latest(@RequestParam String devId) {
        return ApiResult.ok(dataService.latest(devId));
    }

    @GetMapping("/chart")
    public ApiResult<Map<String, Object>> chart(
            @RequestParam String devId,
            @RequestParam String pointCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusHours(24);
        if (end == null) end = LocalDateTime.now();
        return ApiResult.ok(dataService.chart(devId, pointCode, start, end));
    }
}

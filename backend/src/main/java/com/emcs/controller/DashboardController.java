package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.service.DashboardService;
import com.emcs.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final DataService dataService;

    @GetMapping("/summary")
    public ApiResult<Map<String, Object>> summary() {
        return ApiResult.ok(dashboardService.summary());
    }

    @GetMapping("/deviceTypeDist")
    public ApiResult<List<Map<String, Object>>> deviceTypeDist() {
        return ApiResult.ok(dashboardService.deviceTypeDist());
    }

    @GetMapping("/dataTrend")
    public ApiResult<Map<String, Object>> dataTrend(@RequestParam String devId,
                                                    @RequestParam(required = false) String pointCode,
                                                    @RequestParam(defaultValue = "24") int hours) {
        return ApiResult.ok(dashboardService.dataTrend(devId, pointCode, hours));
    }

    @GetMapping("/pointStats")
    public ApiResult<Map<String, Object>> pointStats(
            @RequestParam String devId,
            @RequestParam String pointCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusHours(24);
        if (end == null) end = LocalDateTime.now();
        return ApiResult.ok(dataService.pointStats(devId, pointCode, start, end));
    }
}

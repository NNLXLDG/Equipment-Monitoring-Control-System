package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.common.PageResult;
import com.emcs.dto.ControlSendRequest;
import com.emcs.entity.ControlRecord;
import com.emcs.service.ControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/control")
@RequiredArgsConstructor
public class ControlController {

    private final ControlService controlService;

    @PostMapping("/send")
    @PreAuthorize("hasAuthority('control:send')")
    public ApiResult<Map<String, Object>> send(@RequestBody ControlSendRequest request) {
        return ApiResult.ok(controlService.send(request.getDevId(), request.getData()));
    }

    @GetMapping("/records")
    public ApiResult<PageResult<ControlRecord>> records(@RequestParam(required = false) String devId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(controlService.records(devId, page, size));
    }
}

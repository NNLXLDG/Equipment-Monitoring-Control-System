package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.common.PageResult;
import com.emcs.entity.DevicePoint;
import com.emcs.service.DevicePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class DevicePointController {

    private final DevicePointService pointService;

    @GetMapping
    public ApiResult<PageResult<DevicePoint>> page(@RequestParam(required = false) Long deviceId,
                                                   @RequestParam(required = false) Integer pointType,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(pointService.page(deviceId, pointType, page, size));
    }

    @GetMapping("/{id}")
    public ApiResult<DevicePoint> get(@PathVariable Long id) {
        return ApiResult.ok(pointService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('point:add')")
    public ApiResult<Void> create(@RequestBody DevicePoint point) {
        pointService.create(point);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('point:edit')")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody DevicePoint point) {
        pointService.update(id, point);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('point:delete')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        pointService.delete(id);
        return ApiResult.ok();
    }
}

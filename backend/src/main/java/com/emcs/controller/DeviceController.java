package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.common.PageResult;
import com.emcs.entity.Device;
import com.emcs.entity.DevicePoint;
import com.emcs.service.DevicePointService;
import com.emcs.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DevicePointService pointService;

    @GetMapping
    public ApiResult<PageResult<Device>> page(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String deviceType,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(deviceService.page(keyword, status, deviceType, page, size));
    }

    @GetMapping("/all")
    public ApiResult<List<Device>> all() {
        return ApiResult.ok(deviceService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResult<Device> get(@PathVariable Long id) {
        return ApiResult.ok(deviceService.get(id));
    }

    @GetMapping("/{id}/points")
    public ApiResult<List<DevicePoint>> points(@PathVariable Long id) {
        return ApiResult.ok(pointService.listByDevice(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('device:add')")
    public ApiResult<Void> create(@RequestBody Device device) {
        deviceService.create(device);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('device:edit')")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Device device) {
        deviceService.update(id, device);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('device:delete')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ApiResult.ok();
    }
}

package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.dto.MenuVO;
import com.emcs.entity.Permission;
import com.emcs.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    public ApiResult<List<MenuVO>> tree() {
        return ApiResult.ok(permissionService.tree());
    }

    @GetMapping
    public ApiResult<List<Permission>> list() {
        return ApiResult.ok(permissionService.listAll());
    }

    @PostMapping
    public ApiResult<Void> create(@RequestBody Permission permission) {
        permissionService.create(permission);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Permission permission) {
        permissionService.update(id, permission);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResult.ok();
    }
}

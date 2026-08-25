package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.common.PageResult;
import com.emcs.entity.Role;
import com.emcs.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    /** 无 page 参数返回全量列表（下拉），有 page 参数返回分页 */
    @GetMapping
    public ApiResult<Object> roles(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(defaultValue = "10") int size) {
        if (page == null) {
            return ApiResult.ok(roleService.listAll());
        }
        PageResult<Role> result = roleService.page(keyword, page, size);
        return ApiResult.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResult<Role> get(@PathVariable Long id) {
        return ApiResult.ok(roleService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:add')")
    public ApiResult<Void> create(@RequestBody Role role) {
        roleService.create(role);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:edit')")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Role role) {
        roleService.update(id, role);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResult.ok();
    }

    @GetMapping("/{id}/permissions")
    public ApiResult<List<Long>> permissions(@PathVariable Long id) {
        return ApiResult.ok(roleService.getPermissionIds(id));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:edit')")
    public ApiResult<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ApiResult.ok();
    }
}

package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.common.PageResult;
import com.emcs.dto.UserSaveDTO;
import com.emcs.dto.UserVO;
import com.emcs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResult<PageResult<UserVO>> page(@RequestParam(required = false) String keyword,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(userService.page(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResult<UserVO> get(@PathVariable Long id) {
        return ApiResult.ok(userService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public ApiResult<Void> create(@RequestBody UserSaveDTO dto) {
        userService.create(dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody UserSaveDTO dto) {
        userService.update(id, dto);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResult<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userService.updateStatus(id, body.get("status"));
        return ApiResult.ok();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('user:edit')")
    public ApiResult<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return ApiResult.ok();
    }
}

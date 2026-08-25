package com.emcs.controller;

import com.emcs.common.ApiResult;
import com.emcs.dto.LoginRequest;
import com.emcs.dto.UserInfoVO;
import com.emcs.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/me")
    public ApiResult<UserInfoVO> me() {
        return ApiResult.ok(authService.currentUser());
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        return ApiResult.ok();
    }
}
